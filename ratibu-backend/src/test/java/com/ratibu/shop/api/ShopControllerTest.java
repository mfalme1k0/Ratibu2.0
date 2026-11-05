package com.ratibu.shop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.common.web.ApiExceptionHandler;
import com.ratibu.shop.application.CreateShopCommand;
import com.ratibu.shop.application.CreateShopResult;
import com.ratibu.shop.application.CreateShopService;
import com.ratibu.shop.domain.ShopRepository;
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
        controllers = ShopController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, ShopControllerTest.StubConfig.class})
class ShopControllerTest {

    private static final String BUSINESS_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StubCreateShopService createShopService;

    @BeforeEach
    void reset() {
        createShopService.reset();
    }

    @Test
    void create_returns201() throws Exception {
        createShopService.onExecute(cmd -> new CreateShopResult(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                BUSINESS_ID,
                "Westlands",
                "ACTIVE",
                Instant.parse("2026-11-05T10:00:00Z"),
                false));

        String body = objectMapper.writeValueAsString(new CreateShopRequest(null, "Westlands"));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/shops", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Westlands"))
                .andExpect(jsonPath("$.businessId").value(BUSINESS_ID))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.alreadyExisted").value(false));
    }

    @Test
    void create_returns200_whenIdempotent() throws Exception {
        createShopService.onExecute(cmd -> new CreateShopResult(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                BUSINESS_ID,
                "Westlands",
                "ACTIVE",
                Instant.parse("2026-11-05T10:00:00Z"),
                true));

        String body = objectMapper.writeValueAsString(
                new CreateShopRequest("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "Westlands"));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/shops", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }

    @Test
    void create_returns400_whenNameBlank() throws Exception {
        String body = objectMapper.writeValueAsString(new CreateShopRequest(null, "  "));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/shops", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void create_returns404_whenBusinessMissing() throws Exception {
        createShopService.onExecute(cmd -> {
            throw new NotFoundException("Business not found: " + cmd.businessId());
        });

        String body = objectMapper.writeValueAsString(new CreateShopRequest(null, "Westlands"));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/shops", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    static final class StubCreateShopService extends CreateShopService {

        private Function<CreateShopCommand, CreateShopResult> handler = cmd -> {
            throw new IllegalStateException("Stub not configured");
        };

        StubCreateShopService() {
            super(null, null);
        }

        void onExecute(Function<CreateShopCommand, CreateShopResult> handler) {
            this.handler = handler;
        }

        void reset() {
            this.handler = cmd -> {
                throw new IllegalStateException("Stub not configured");
            };
        }

        @Override
        public CreateShopResult execute(CreateShopCommand command) {
            return handler.apply(command);
        }
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        StubCreateShopService stubCreateShopService() {
            return new StubCreateShopService();
        }
    }
}
