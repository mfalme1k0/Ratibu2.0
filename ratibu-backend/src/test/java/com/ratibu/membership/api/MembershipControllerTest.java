package com.ratibu.membership.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.common.web.ApiExceptionHandler;
import com.ratibu.membership.application.CreateMembershipCommand;
import com.ratibu.membership.application.CreateMembershipResult;
import com.ratibu.membership.application.CreateMembershipService;
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
        controllers = MembershipController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, MembershipControllerTest.StubConfig.class})
class MembershipControllerTest {

    private static final String BUSINESS_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private static final String ACCOUNT_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StubCreateMembershipService service;

    @BeforeEach
    void reset() {
        service.reset();
    }

    @Test
    void create_returns201() throws Exception {
        service.onExecute(cmd -> new CreateMembershipResult(
                "cccccccc-cccc-cccc-cccc-cccccccccccc",
                ACCOUNT_ID,
                BUSINESS_ID,
                "OWNER",
                Instant.parse("2026-11-05T12:00:00Z"),
                false));

        String body = objectMapper.writeValueAsString(
                new CreateMembershipRequest(null, ACCOUNT_ID, "OWNER"));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/memberships", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.accountId").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.businessId").value(BUSINESS_ID));
    }

    @Test
    void create_returns404_whenAccountMissing() throws Exception {
        service.onExecute(cmd -> {
            throw new NotFoundException("Account not found: " + cmd.accountId());
        });

        String body = objectMapper.writeValueAsString(
                new CreateMembershipRequest(null, ACCOUNT_ID, "OWNER"));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/memberships", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void create_returns400_whenRoleMissing() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateMembershipRequest(null, ACCOUNT_ID, "  "));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/memberships", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    static final class StubCreateMembershipService extends CreateMembershipService {
        private Function<CreateMembershipCommand, CreateMembershipResult> handler = cmd -> {
            throw new IllegalStateException("not configured");
        };

        StubCreateMembershipService() {
            super(null, null, null);
        }

        void onExecute(Function<CreateMembershipCommand, CreateMembershipResult> h) {
            this.handler = h;
        }

        void reset() {
            this.handler = cmd -> {
                throw new IllegalStateException("not configured");
            };
        }

        @Override
        public CreateMembershipResult execute(CreateMembershipCommand command) {
            return handler.apply(command);
        }
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        StubCreateMembershipService stub() {
            return new StubCreateMembershipService();
        }
    }
}
