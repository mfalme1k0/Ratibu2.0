package com.ratibu.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack tests for the Business api: HTTP (MockMvc, real security filter
 * chain) -> controller -> application service -> domain -> JPA adapter ->
 * Flyway-migrated H2. Nothing is mocked or stubbed, and no Mockito is used
 * (Byte Buddy in Spring Boot 3.3.4 cannot instrument classes on Java 26).
 *
 * <p>Every claim about stored data is checked with raw JDBC, not through the
 * repository, so the JPA mapping is not marking its own homework. Tests are
 * not {@code @Transactional}: each request commits, as in production.
 *
 * <p>Uses the {@code e2e} profile (its own H2 database, real SecurityConfig).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
class BusinessApiE2ETest {

    private static final String URL = "/api/v1/businesses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM business");
    }

    // ------------------------------------------------------------------ helpers

    private MvcResult post(String rawJson) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andReturn();
    }

    private MvcResult post(String businessId, String name, String currency) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        if (businessId != null) {
            body.put("businessId", businessId);
        }
        body.put("name", name);
        body.put("currency", currency);
        return post(objectMapper.writeValueAsString(body));
    }

    private JsonNode bodyOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long rowCount() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM business", Long.class);
        return count == null ? 0 : count;
    }

    private String storedName(String id) {
        return jdbc.queryForObject("SELECT name FROM business WHERE id = ?", String.class, UUID.fromString(id));
    }

    private String storedCurrency(String id) {
        return jdbc.queryForObject("SELECT currency FROM business WHERE id = ?", String.class, UUID.fromString(id));
    }

    private Instant storedCreatedAt(String id) {
        return jdbc.queryForObject(
                        "SELECT created_at FROM business WHERE id = ?", OffsetDateTime.class, UUID.fromString(id))
                .toInstant();
    }

    /**
     * The service answers a fresh create with the in-memory Instant (nanosecond
     * precision on modern JDKs) while the database keeps microseconds, so the
     * same moment can differ by under 1 microsecond between a 201 and a replay.
     */
    private static void assertSameMoment(Instant a, Instant b) {
        assertThat(Duration.between(a, b).abs()).isLessThanOrEqualTo(Duration.ofNanos(1_000));
    }

    // ------------------------------------------------------------------ create

    @Nested
    class Create {

        @Test
        void returns201_andTheStoredRowMatchesTheResponse() throws Exception {
            MvcResult result = post(null, "John's Barbershop", "KES");

            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            JsonNode body = bodyOf(result);
            String id = body.get("businessId").asText();

            assertThat(UUID.fromString(id)).isNotNull();
            assertThat(body.get("name").asText()).isEqualTo("John's Barbershop");
            assertThat(body.get("currency").asText()).isEqualTo("KES");
            assertThat(body.get("alreadyExisted").asBoolean()).isFalse();

            assertThat(rowCount()).isEqualTo(1);
            assertThat(storedName(id)).isEqualTo("John's Barbershop");
            assertThat(storedCurrency(id)).isEqualTo("KES");
            assertSameMoment(Instant.parse(body.get("createdAt").asText()), storedCreatedAt(id));
        }

        @Test
        void serverGeneratedIdsAreUnique_andTheSameNameIsAllowed() throws Exception {
            JsonNode first = bodyOf(post(null, "Barbershop", "KES"));
            JsonNode second = bodyOf(post(null, "Barbershop", "KES"));

            assertThat(first.get("businessId").asText()).isNotEqualTo(second.get("businessId").asText());
            assertThat(first.get("alreadyExisted").asBoolean()).isFalse();
            assertThat(second.get("alreadyExisted").asBoolean()).isFalse();
            assertThat(rowCount()).isEqualTo(2);
        }

        @Test
        void nameIsTrimmedAndCurrencyUpperCased_inTheResponseAndInTheDatabase() throws Exception {
            MvcResult result = post(null, "  Padded Name  ", "kes");

            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            JsonNode body = bodyOf(result);
            String id = body.get("businessId").asText();

            assertThat(body.get("name").asText()).isEqualTo("Padded Name");
            assertThat(body.get("currency").asText()).isEqualTo("KES");
            assertThat(storedName(id)).isEqualTo("Padded Name");
            assertThat(storedCurrency(id)).isEqualTo("KES");
        }

        @Test
        void nameOfExactlyMaxLength_isStored() throws Exception {
            String max = "a".repeat(120);
            MvcResult result = post(null, max, "KES");

            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            assertThat(storedName(bodyOf(result).get("businessId").asText())).isEqualTo(max);
        }

        @Test
        void unicodeNameSurvivesTheWholeRoundTrip() throws Exception {
            String name = "Mama Fua's Salon — Kibera / 美容院 ✂";
            MvcResult result = post(null, name, "KES");

            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            JsonNode body = bodyOf(result);
            assertThat(body.get("name").asText()).isEqualTo(name);
            assertThat(storedName(body.get("businessId").asText())).isEqualTo(name);
        }

        @Test
        void everySupportedCurrencyIsAccepted() throws Exception {
            for (String code : List.of("KES", "USD", "EUR", "GBP", "UGX", "TZS", "RWF")) {
                MvcResult result = post(null, "Shop " + code, code);
                assertThat(result.getResponse().getStatus()).as(code).isEqualTo(201);
                assertThat(storedCurrency(bodyOf(result).get("businessId").asText())).isEqualTo(code);
            }
            assertThat(rowCount()).isEqualTo(7);
        }
    }

    // ------------------------------------------------------------------ offline-sync replay (ADR-010)

    @Nested
    class OfflineSyncReplay {

        private static final String CLIENT_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

        @Test
        void clientIdIsHonoured_andReplayReturns200_withoutDuplicating() throws Exception {
            MvcResult first = post(CLIENT_ID, "Mary's Salon", "USD");
            MvcResult replay = post(CLIENT_ID, "Mary's Salon", "USD");

            assertThat(first.getResponse().getStatus()).isEqualTo(201);
            assertThat(replay.getResponse().getStatus()).isEqualTo(200);

            JsonNode firstBody = bodyOf(first);
            JsonNode replayBody = bodyOf(replay);
            assertThat(firstBody.get("businessId").asText()).isEqualTo(CLIENT_ID);
            assertThat(replayBody.get("businessId").asText()).isEqualTo(CLIENT_ID);
            assertThat(firstBody.get("alreadyExisted").asBoolean()).isFalse();
            assertThat(replayBody.get("alreadyExisted").asBoolean()).isTrue();
            assertSameMoment(
                    Instant.parse(firstBody.get("createdAt").asText()),
                    Instant.parse(replayBody.get("createdAt").asText()));
            assertThat(rowCount()).isEqualTo(1);
        }

        @Test
        void replayWithADifferentPayload_returnsTheOriginal_andOverwritesNothing() throws Exception {
            post(CLIENT_ID, "Original Name", "KES");

            MvcResult replay = post(CLIENT_ID, "Changed Name", "USD");

            // Current contract: same id means "already created", whatever else the payload says.
            assertThat(replay.getResponse().getStatus()).isEqualTo(200);
            JsonNode body = bodyOf(replay);
            assertThat(body.get("name").asText()).isEqualTo("Original Name");
            assertThat(body.get("currency").asText()).isEqualTo("KES");
            assertThat(body.get("alreadyExisted").asBoolean()).isTrue();

            assertThat(storedName(CLIENT_ID)).isEqualTo("Original Name");
            assertThat(storedCurrency(CLIENT_ID)).isEqualTo("KES");
            assertThat(rowCount()).isEqualTo(1);
        }

        @Test
        void upperCaseIdMatchesTheLowerCaseOriginal() throws Exception {
            post(CLIENT_ID, "Shop", "KES");

            MvcResult replay = post(CLIENT_ID.toUpperCase(), "Shop", "KES");

            assertThat(replay.getResponse().getStatus()).isEqualTo(200);
            assertThat(bodyOf(replay).get("businessId").asText()).isEqualTo(CLIENT_ID);
            assertThat(rowCount()).isEqualTo(1);
        }

        @Test
        void blankBusinessIdIsTreatedAsAbsent_andTheServerGeneratesOne() throws Exception {
            MvcResult result = post("   ", "Shop", "KES");

            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            assertThat(UUID.fromString(bodyOf(result).get("businessId").asText())).isNotNull();
            assertThat(rowCount()).isEqualTo(1);
        }

        /**
         * Documents current behaviour, not necessarily desired behaviour:
         * {@code java.util.UUID.fromString} is lenient and pads short groups,
         * so "1-1-1-1-1" is accepted and the response carries a different
         * (canonical) id than the client sent. Strict parsing in StableId would
         * turn this into a 400; update this test if you add it.
         */
        @Test
        void lenientUuidParsing_rewritesTheClientsId() throws Exception {
            MvcResult result = post("1-1-1-1-1", "Shop", "KES");

            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            assertThat(bodyOf(result).get("businessId").asText())
                    .isEqualTo("00000001-0001-0001-0001-000000000001");
        }

        /**
         * KNOWN GAP, disabled on purpose. The service does find-then-insert, so
         * two simultaneous deliveries of the same offline create can both pass
         * the "does it exist?" check and one then fails on the primary key. No
         * handler maps DataIntegrityViolationException, so the client sees a
         * 500 instead of the idempotent 200. Enable this once the service
         * catches the duplicate-key error and re-reads the row.
         */
        @Disabled("Known gap: concurrent replays of the same client id can surface a 500")
        @Test
        void concurrentReplaysOfTheSameId_yieldOne201_theRest200_andOneRow() throws Exception {
            int callers = 8;
            ExecutorService pool = Executors.newFixedThreadPool(callers);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < callers; i++) {
                Callable<Integer> call = () -> {
                    start.await();
                    try {
                        return post(CLIENT_ID, "Race", "KES").getResponse().getStatus();
                    } catch (Exception e) {
                        return 500;
                    }
                };
                futures.add(pool.submit(call));
            }
            start.countDown();

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : futures) {
                statuses.add(f.get(30, TimeUnit.SECONDS));
            }
            pool.shutdown();

            assertThat(statuses).containsOnly(200, 201);
            assertThat(statuses.stream().filter(s -> s == 201).count()).isEqualTo(1);
            assertThat(rowCount()).isEqualTo(1);
        }
    }

    // ------------------------------------------------------------------ rejected input never writes

    static Stream<Arguments> rejectedBodies() {
        return Stream.of(
                Arguments.of("blank name", "{\"name\":\"   \",\"currency\":\"KES\"}", "Validation Failed"),
                Arguments.of("missing name", "{\"currency\":\"KES\"}", "Validation Failed"),
                Arguments.of("name over 120 chars",
                        "{\"name\":\"" + "a".repeat(121) + "\",\"currency\":\"KES\"}", "Validation Failed"),
                Arguments.of("missing currency", "{\"name\":\"Shop\"}", "Validation Failed"),
                Arguments.of("currency too short", "{\"name\":\"Shop\",\"currency\":\"KE\"}", "Validation Failed"),
                Arguments.of("unsupported currency JPY", "{\"name\":\"Shop\",\"currency\":\"JPY\"}", "Invariant Violation"),
                Arguments.of("unsupported currency XXX", "{\"name\":\"Shop\",\"currency\":\"XXX\"}", "Invariant Violation"),
                Arguments.of("non-alphabetic currency", "{\"name\":\"Shop\",\"currency\":\"12A\"}", "Invariant Violation"),
                Arguments.of("malformed business id",
                        "{\"businessId\":\"not-a-uuid\",\"name\":\"Shop\",\"currency\":\"KES\"}", "Bad Request")
        );
    }

    @Nested
    class Rejections {

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.ratibu.business.BusinessApiE2ETest#rejectedBodies")
        void returns400_withTheRightErrorType_andWritesNothing(
                String description, String body, String expectedError) throws Exception {
            MvcResult result = post(body);

            assertThat(result.getResponse().getStatus()).isEqualTo(400);
            JsonNode error = bodyOf(result);
            assertThat(error.get("error").asText()).isEqualTo(expectedError);
            assertThat(error.get("path").asText()).isEqualTo(URL);
            assertThat(rowCount()).isZero();
        }

        @Test
        void unsupportedCurrency_namesTheOffendingCode() throws Exception {
            JsonNode error = bodyOf(post(null, "Shop", "JPY"));

            assertThat(error.get("message").asText()).contains("JPY");
        }

        @Test
        void aRejectedReplay_doesNotDisturbTheExistingRow() throws Exception {
            String id = "11111111-2222-3333-4444-555555555555";
            post(id, "Keep Me", "KES");

            MvcResult bad = post(id, "   ", "KES");

            assertThat(bad.getResponse().getStatus()).isEqualTo(400);
            assertThat(storedName(id)).isEqualTo("Keep Me");
            assertThat(rowCount()).isEqualTo(1);
        }

        @Test
        void malformedJson_returns400_andWritesNothing() throws Exception {
            assertThat(post("{\"name\": \"Shop\", ").getResponse().getStatus()).isEqualTo(400);
            assertThat(rowCount()).isZero();
        }

        @Test
        void nonJsonContentType_returns415() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("name=Shop&currency=KES"))
                    .andExpect(status().isUnsupportedMediaType());
            assertThat(rowCount()).isZero();
        }
    }

    // ------------------------------------------------------------------ real security filter chain

    @Nested
    class SecurityStack {

        @Test
        void anonymousPost_withNoCsrfToken_isAllowed() throws Exception {
            assertThat(post(null, "Shop", "KES").getResponse().getStatus()).isEqualTo(201);
        }

        @Test
        void anonymousRequestToAnyOtherPath_isRejectedWith401_andBasicChallenge() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/businesses/" + UUID.randomUUID()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", startsWith("Basic")));
        }

        @Test
        void anonymousPutOnTheBusinessesPath_isRejectedWith401_andWritesNothing() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.put(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Shop\",\"currency\":\"KES\"}"))
                    .andExpect(status().isUnauthorized());
            assertThat(rowCount()).isZero();
        }
    }
}