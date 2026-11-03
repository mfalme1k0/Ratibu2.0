package com.ratibu.business.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratibu.business.application.CreateBusinessCommand;
import com.ratibu.business.application.CreateBusinessResult;
import com.ratibu.business.application.CreateBusinessService;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.web.ApiExceptionHandler;
import com.ratibu.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for the Business api package: {@link BusinessController},
 * {@link CreateBusinessRequest}, {@link BusinessResponse}, and the shared
 * {@link ApiExceptionHandler} / {@code ApiError} contract.
 *
 *
 * <p>The real {@link SecurityConfig} is imported so the "POST is open, anything
 * else is authenticated" rule is covered here too. The database is not
 * involved; persistence is covered by {@code BusinessE2ETest} and
 * {@code BusinessApiE2ETest}.
 */
@WebMvcTest(controllers = BusinessController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class, BusinessControllerTest.StubConfig.class})
class BusinessControllerTest {

    private static final String URL = "/api/v1/businesses";
    private static final String NEW_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    private static final Instant CREATED_AT = Instant.parse("2026-11-02T12:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StubCreateBusinessService service;

    @BeforeEach
    void resetStub() {
        service.reset();
    }

    // ------------------------------------------------------------------ helpers

    private ResultActions post(String json) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private String json(String businessId, String name, String currency) throws Exception {
        return objectMapper.writeValueAsString(new CreateBusinessRequest(businessId, name, currency));
    }

    // ------------------------------------------------------------------ success paths

    @Nested
    class Success {

        @Test
        void newBusiness_returns201_withFullBody() throws Exception {
            service.respondWith(cmd -> new CreateBusinessResult(
                    NEW_ID, "John's Barbershop", "KES", CREATED_AT, false));

            post(json(null, "John's Barbershop", "KES"))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.businessId").value(NEW_ID))
                    .andExpect(jsonPath("$.name").value("John's Barbershop"))
                    .andExpect(jsonPath("$.currency").value("KES"))
                    .andExpect(jsonPath("$.createdAt").value("2026-11-02T12:00:00Z"))
                    .andExpect(jsonPath("$.alreadyExisted").value(false));
        }

        @Test
        void idempotentReplay_returns200_withAlreadyExistedTrue() throws Exception {
            service.respondWith(cmd -> new CreateBusinessResult(
                    "11111111-2222-3333-4444-555555555555", "Mary's Salon", "USD", CREATED_AT, true));

            post(json("11111111-2222-3333-4444-555555555555", "Mary's Salon", "USD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.businessId").value("11111111-2222-3333-4444-555555555555"))
                    .andExpect(jsonPath("$.alreadyExisted").value(true));
        }

        @Test
        void responseExposesExactlyTheFiveDocumentedFields() throws Exception {
            MvcResult result = post(json(null, "Shop", "KES"))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
            List<String> fields = new ArrayList<>();
            body.fieldNames().forEachRemaining(fields::add);

            assertThat(fields).containsExactlyInAnyOrder(
                    "businessId", "name", "currency", "createdAt", "alreadyExisted");
        }

        @Test
        void nameAtMaxLength_isAccepted() throws Exception {
            post(json(null, "a".repeat(120), "KES"))
                    .andExpect(status().isCreated());
            assertThat(service.received()).hasSize(1);
        }

        @Test
        void unknownJsonFields_areIgnored() throws Exception {
            post("{\"name\":\"Shop\",\"currency\":\"KES\",\"somethingNew\":true}")
                    .andExpect(status().isCreated());
            assertThat(service.received()).hasSize(1);
        }
    }

    // ------------------------------------------------------------------ request -> command mapping

    @Nested
    class CommandMapping {

        @Test
        void withoutBusinessId_commandCarriesEmptyOptional() throws Exception {
            post(json(null, "Shop", "KES")).andExpect(status().isCreated());

            CreateBusinessCommand command = service.onlyReceived();
            assertThat(command.businessId()).isEmpty();
            assertThat(command.name()).isEqualTo("Shop");
            assertThat(command.currency()).isEqualTo("KES");
        }

        @Test
        void withBusinessId_commandCarriesIt() throws Exception {
            post(json(NEW_ID, "Shop", "KES")).andExpect(status().isCreated());

            assertThat(service.onlyReceived().businessId()).isEqualTo(Optional.of(NEW_ID));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "    "})
        void blankBusinessId_isTreatedAsAbsent(String blank) throws Exception {
            post(json(blank, "Shop", "KES")).andExpect(status().isCreated());

            assertThat(service.onlyReceived().businessId()).isEmpty();
        }

        @Test
        void nameAndCurrency_arePassedThroughVerbatim_normalisationIsTheDomainsJob() throws Exception {
            post(json(null, "  Padded Name  ", "kes")).andExpect(status().isCreated());

            CreateBusinessCommand command = service.onlyReceived();
            assertThat(command.name()).isEqualTo("  Padded Name  ");
            assertThat(command.currency()).isEqualTo("kes");
        }
    }

    // ------------------------------------------------------------------ bean validation -> 400

    static Stream<Arguments> invalidBodies() {
        String ok = "\"currency\":\"KES\"";
        String currencyMsg = "currency: currency must be a 3-letter ISO 4217 code";
        return Stream.of(
                Arguments.of("missing name", "{" + ok + "}", "name: name is required"),
                Arguments.of("null name", "{\"name\":null," + ok + "}", "name: name is required"),
                Arguments.of("blank name", "{\"name\":\"   \"," + ok + "}", "name: name is required"),
                Arguments.of("name over 120 chars",
                        "{\"name\":\"" + "a".repeat(121) + "\"," + ok + "}",
                        "name: name must be at most 120 characters"),
                Arguments.of("missing currency", "{\"name\":\"Shop\"}", "currency: currency is required"),
                Arguments.of("empty currency", "{\"name\":\"Shop\",\"currency\":\"\"}", "currency: currency is required"),
                Arguments.of("currency too short", "{\"name\":\"Shop\",\"currency\":\"KE\"}", currencyMsg),
                Arguments.of("currency too long", "{\"name\":\"Shop\",\"currency\":\"KESS\"}", currencyMsg),
                Arguments.of("businessId over 36 chars",
                        "{\"businessId\":\"" + "x".repeat(37) + "\",\"name\":\"Shop\"," + ok + "}",
                        "businessId: businessId must be a UUID string")
        );
    }

    @Nested
    class Validation {

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.ratibu.business.api.BusinessControllerTest#invalidBodies")
        void invalidRequest_returns400_listsTheFieldError_andNeverReachesTheService(
                String description, String body, String expectedDetail) throws Exception {
            post(body)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.details", hasItem(expectedDetail)));

            assertThat(service.received()).isEmpty();
        }

        @Test
        void emptyObject_reportsBothRequiredFields() throws Exception {
            post("{}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", containsInAnyOrder(
                            "name: name is required",
                            "currency: currency is required")));
        }

        @Test
        void errorEnvelope_hasTheDocumentedShape() throws Exception {
            post("{}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.message").value("Request validation failed"))
                    .andExpect(jsonPath("$.path").value(URL))
                    .andExpect(jsonPath("$.timestamp").isString())
                    .andExpect(jsonPath("$.details").isArray());
        }
    }

    // ----------------------------------------- exception translation

    @Nested
    class ExceptionTranslation {

        @Test
        void invariantViolation_maps_to400_withMessagePassedThrough() throws Exception {
            service.respondWith(cmd -> {
                throw new InvariantViolationException("Currency code is not supported in MVP: JPY");
            });

            post(json(null, "Shop", "JPY"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Invariant Violation"))
                    .andExpect(jsonPath("$.message").value("Currency code is not supported in MVP: JPY"))
                    .andExpect(jsonPath("$.path").value(URL))
                    .andExpect(jsonPath("$.details").isEmpty());
        }

        @Test
        void illegalArgument_maps_to400BadRequest() throws Exception {
            // What StableId.fromString("not-a-uuid") raises inside the real service.
            service.respondWith(cmd -> {
                throw new IllegalArgumentException("Invalid UUID string: not-a-uuid");
            });

            post(json("not-a-uuid", "Shop", "KES"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Invalid UUID string: not-a-uuid"));
        }
    }

    // ---------------------------------------------- malformed HTTP

    @Nested
    class MalformedRequests {

        @Test
        void malformedJson_returns400_andNeverReachesTheService() throws Exception {
            post("{\"name\": \"Shop\", \"currency\": ")
                    .andExpect(status().isBadRequest());
            assertThat(service.received()).isEmpty();
        }

        @Test
        void emptyBody_returns400() throws Exception {
            post("").andExpect(status().isBadRequest());
            assertThat(service.received()).isEmpty();
        }

        @Test
        void nonJsonContentType_returns415() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("name=Shop&currency=KES"))
                    .andExpect(status().isUnsupportedMediaType());
            assertThat(service.received()).isEmpty();
        }
    }

    // --------------------------------------- security rules

    @Nested
    class Security {

        @Test
        void anonymousPost_isPermitted_andNeedsNoCsrfToken() throws Exception {
            // No credentials, no CSRF token: the create-business path is open for the prototype slice.
            post(json(null, "Shop", "KES")).andExpect(status().isCreated());
        }

        @Test
        void anonymousRequestToAnyOtherPath_isRejectedWith401() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/anything"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", startsWith("Basic")));
        }

        @Test
        void anonymousNonPostOnTheBusinessesPath_isRejectedWith401() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.put(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(null, "Shop", "KES")))
                    .andExpect(status().isUnauthorized());
            assertThat(service.received()).isEmpty();
        }
    }

    // -------------------------------------------- test double

    @TestConfiguration(proxyBeanMethods = false)
    static class StubConfig {
        @Bean
        StubCreateBusinessService createBusinessService() {
            return new StubCreateBusinessService();
        }
    }

    /**
     * Hand-written replacement for the application service. By default it
     * echoes the command back as a successful create, so tests only override
     * behaviour when they care about it.
     */
    static final class StubCreateBusinessService extends CreateBusinessService {

        private final List<CreateBusinessCommand> received = new ArrayList<>();
        private Function<CreateBusinessCommand, CreateBusinessResult> handler = StubCreateBusinessService::echo;

        StubCreateBusinessService() {
            super(null); // repository is never touched: execute() is fully overridden
        }

        @Override
        public CreateBusinessResult execute(CreateBusinessCommand command) {
            received.add(command);
            return handler.apply(command);
        }

        void respondWith(Function<CreateBusinessCommand, CreateBusinessResult> newHandler) {
            this.handler = newHandler;
        }

        void reset() {
            received.clear();
            handler = StubCreateBusinessService::echo;
        }

        List<CreateBusinessCommand> received() {
            return List.copyOf(received);
        }

        CreateBusinessCommand onlyReceived() {
            assertThat(received).as("commands received by the service").hasSize(1);
            return received.getFirst();
        }

        private static CreateBusinessResult echo(CreateBusinessCommand command) {
            return new CreateBusinessResult(
                    command.businessId().orElse(NEW_ID),
                    command.name(),
                    command.currency(),
                    CREATED_AT,
                    false);
        }
    }
}