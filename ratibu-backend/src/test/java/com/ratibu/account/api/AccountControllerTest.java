package com.ratibu.account.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratibu.account.application.CreateAccountCommand;
import com.ratibu.account.application.CreateAccountResult;
import com.ratibu.account.application.CreateAccountService;
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
        controllers = AccountController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, AccountControllerTest.StubConfig.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StubCreateAccountService service;

    @BeforeEach
    void reset() {
        service.reset();
    }

    @Test
    void create_returns201() throws Exception {
        service.onExecute(cmd -> new CreateAccountResult(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "jane@example.com",
                "Jane",
                Instant.parse("2026-11-05T08:00:00Z"),
                false));

        String body = objectMapper.writeValueAsString(
                new CreateAccountRequest(null, "jane@example.com", "Jane"));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.alreadyExisted").value(false));
    }

    @Test
    void create_returns400_whenEmailMissing() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateAccountRequest(null, "  ", null));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    static final class StubCreateAccountService extends CreateAccountService {
        private Function<CreateAccountCommand, CreateAccountResult> handler = cmd -> {
            throw new IllegalStateException("not configured");
        };

        StubCreateAccountService() {
            super(null);
        }

        void onExecute(Function<CreateAccountCommand, CreateAccountResult> h) {
            this.handler = h;
        }

        void reset() {
            this.handler = cmd -> {
                throw new IllegalStateException("not configured");
            };
        }

        @Override
        public CreateAccountResult execute(CreateAccountCommand command) {
            return handler.apply(command);
        }
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        StubCreateAccountService stub() {
            return new StubCreateAccountService();
        }
    }
}
