package com.ratibu.catalog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratibu.catalog.application.CreateCatalogServiceCommand;
import com.ratibu.catalog.application.CreateCatalogServiceResult;
import com.ratibu.catalog.application.CreateCatalogServiceService;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.common.web.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CatalogServiceController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, CatalogServiceControllerTest.StubConfig.class})
class CatalogServiceControllerTest {

    private static final String BUSINESS_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StubCreateCatalogServiceService service;

    @BeforeEach
    void reset() {
        service.reset();
    }

    @Test
    void create_returns201() throws Exception {
        service.onExecute(cmd -> new CreateCatalogServiceResult(
                "55555555-5555-5555-5555-555555555555",
                BUSINESS_ID,
                "Haircut",
                null,
                new BigDecimal("500.00"),
                "ACTIVE",
                Instant.parse("2026-11-05T12:00:00Z"),
                false));

        String body = objectMapper.writeValueAsString(
                new CreateCatalogServiceRequest(null, "Haircut", null, new BigDecimal("500.00")));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/services", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Haircut"))
                .andExpect(jsonPath("$.defaultPrice").value(500.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_missingPrice_returns400() throws Exception {
        String body = """
                {"name":"Haircut"}
                """;

        mockMvc.perform(post("/api/v1/businesses/{businessId}/services", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_unknownBusiness_returns404() throws Exception {
        service.onExecute(cmd -> {
            throw new NotFoundException("Business not found: " + cmd.businessId());
        });

        String body = objectMapper.writeValueAsString(
                new CreateCatalogServiceRequest(null, "Haircut", null, BigDecimal.TEN));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/services", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        StubCreateCatalogServiceService createCatalogServiceService() {
            return new StubCreateCatalogServiceService();
        }
    }

    static final class StubCreateCatalogServiceService extends CreateCatalogServiceService {
        private Function<CreateCatalogServiceCommand, CreateCatalogServiceResult> handler =
                cmd -> new CreateCatalogServiceResult(
                        "id", "biz", "n", null, BigDecimal.ONE, "ACTIVE", Instant.now(), false);

        StubCreateCatalogServiceService() {
            super(null, null);
        }

        void onExecute(Function<CreateCatalogServiceCommand, CreateCatalogServiceResult> handler) {
            this.handler = handler;
        }

        void reset() {
            this.handler = cmd -> new CreateCatalogServiceResult(
                    "id", "biz", "n", null, BigDecimal.ONE, "ACTIVE", Instant.now(), false);
        }

        @Override
        public CreateCatalogServiceResult execute(CreateCatalogServiceCommand command) {
            return handler.apply(command);
        }
    }
}
