package com.ratibu.worker.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.common.web.ApiExceptionHandler;
import com.ratibu.worker.application.CreateWorkerCommand;
import com.ratibu.worker.application.CreateWorkerResult;
import com.ratibu.worker.application.CreateWorkerService;
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
        controllers = WorkerController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, WorkerControllerTest.StubConfig.class})
class WorkerControllerTest {

    private static final String BUSINESS_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StubCreateWorkerService service;

    @BeforeEach
    void reset() {
        service.reset();
    }

    @Test
    void create_returns201() throws Exception {
        service.onExecute(cmd -> new CreateWorkerResult(
                "22222222-2222-2222-2222-222222222222",
                BUSINESS_ID,
                "Mary",
                null,
                "ACTIVE",
                Instant.parse("2026-11-05T12:00:00Z"),
                false));

        String body = objectMapper.writeValueAsString(
                new CreateWorkerRequest(null, "Mary", null));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/workers", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mary"))
                .andExpect(jsonPath("$.businessId").value(BUSINESS_ID))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.alreadyExisted").value(false));
    }

    @Test
    void create_idempotent_returns200() throws Exception {
        service.onExecute(cmd -> new CreateWorkerResult(
                "22222222-2222-2222-2222-222222222222",
                BUSINESS_ID,
                "Mary",
                null,
                "ACTIVE",
                Instant.parse("2026-11-05T12:00:00Z"),
                true));

        String body = objectMapper.writeValueAsString(
                new CreateWorkerRequest("22222222-2222-2222-2222-222222222222", "Mary", null));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/workers", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateWorkerRequest(null, "", null));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/workers", BUSINESS_ID)
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
                new CreateWorkerRequest(null, "Mary", null));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/workers", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        StubCreateWorkerService createWorkerService() {
            return new StubCreateWorkerService();
        }
    }

    static final class StubCreateWorkerService extends CreateWorkerService {
        private Function<CreateWorkerCommand, CreateWorkerResult> handler =
                cmd -> new CreateWorkerResult("id", "biz", "n", null, "ACTIVE", Instant.now(), false);

        StubCreateWorkerService() {
            super(null, null, null);
        }

        void onExecute(Function<CreateWorkerCommand, CreateWorkerResult> handler) {
            this.handler = handler;
        }

        void reset() {
            this.handler = cmd -> new CreateWorkerResult("id", "biz", "n", null, "ACTIVE", Instant.now(), false);
        }

        @Override
        public CreateWorkerResult execute(CreateWorkerCommand command) {
            return handler.apply(command);
        }
    }
}
