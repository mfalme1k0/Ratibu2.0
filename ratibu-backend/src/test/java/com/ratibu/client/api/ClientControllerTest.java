package com.ratibu.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratibu.client.application.CreateClientCommand;
import com.ratibu.client.application.CreateClientResult;
import com.ratibu.client.application.CreateClientService;
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

import java.time.Instant;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ClientController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, ClientControllerTest.StubConfig.class})
class ClientControllerTest {

    private static final String BUSINESS_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StubCreateClientService service;

    @BeforeEach
    void reset() {
        service.reset();
    }

    @Test
    void create_returns201() throws Exception {
        service.onExecute(cmd -> new CreateClientResult(
                "33333333-3333-3333-3333-333333333333",
                BUSINESS_ID,
                "Jane",
                null,
                null,
                null,
                "ACTIVE",
                Instant.parse("2026-11-05T12:00:00Z"),
                false));

        String body = objectMapper.writeValueAsString(
                new CreateClientRequest(null, "Jane", null, null, null));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.businessId").value(BUSINESS_ID))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.alreadyExisted").value(false));
    }

    @Test
    void create_idempotent_returns200() throws Exception {
        service.onExecute(cmd -> new CreateClientResult(
                "33333333-3333-3333-3333-333333333333",
                BUSINESS_ID,
                "Jane",
                null,
                null,
                null,
                "ACTIVE",
                Instant.parse("2026-11-05T12:00:00Z"),
                true));

        String body = objectMapper.writeValueAsString(
                new CreateClientRequest("33333333-3333-3333-3333-333333333333", "Jane", null, null, null));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateClientRequest(null, "", null, null, null));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", BUSINESS_ID)
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
                new CreateClientRequest(null, "Jane", null, null, null));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        StubCreateClientService createClientService() {
            return new StubCreateClientService();
        }
    }

    static final class StubCreateClientService extends CreateClientService {
        private Function<CreateClientCommand, CreateClientResult> handler =
                cmd -> new CreateClientResult("id", "biz", "n", null, null, null, "ACTIVE", Instant.now(), false);

        StubCreateClientService() {
            super(null, null);
        }

        void onExecute(Function<CreateClientCommand, CreateClientResult> handler) {
            this.handler = handler;
        }

        void reset() {
            this.handler = cmd -> new CreateClientResult(
                    "id", "biz", "n", null, null, null, "ACTIVE", Instant.now(), false);
        }

        @Override
        public CreateClientResult execute(CreateClientCommand command) {
            return handler.apply(command);
        }
    }
}
