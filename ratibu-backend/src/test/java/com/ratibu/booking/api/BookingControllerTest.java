package com.ratibu.booking.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratibu.booking.application.CreateBookingCommand;
import com.ratibu.booking.application.CreateBookingResult;
import com.ratibu.booking.application.CreateBookingService;
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
        controllers = BookingController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, BookingControllerTest.StubConfig.class})
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private StubCreateBookingService service;

    @BeforeEach
    void reset() {
        service.reset();
    }

    @Test
    void create_returns201() throws Exception {
        service.onExecute(cmd -> new CreateBookingResult(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                cmd.shopId(), cmd.clientId(), cmd.serviceId(), null,
                cmd.scheduledAt(), 30, new BigDecimal("500.00"), null,
                "SCHEDULED", Instant.parse("2026-11-05T12:00:00Z"), false));

        String body = """
                {
                  "shopId":"11111111-1111-1111-1111-111111111111",
                  "clientId":"22222222-2222-2222-2222-222222222222",
                  "serviceId":"33333333-3333-3333-3333-333333333333",
                  "scheduledAt":"2026-12-01T10:00:00Z",
                  "durationMinutes":30,
                  "expectedPrice":500.00
                }
                """;

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.expectedPrice").value(500.00));
    }

    @Test
    void create_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        StubCreateBookingService createBookingService() {
            return new StubCreateBookingService();
        }
    }

    static final class StubCreateBookingService extends CreateBookingService {
        private Function<CreateBookingCommand, CreateBookingResult> handler =
                cmd -> new CreateBookingResult(
                        "id", "s", "c", "svc", null, Instant.now(), null,
                        BigDecimal.ONE, null, "SCHEDULED", Instant.now(), false);

        StubCreateBookingService() {
            super(null, null, null, null, null);
        }

        void onExecute(Function<CreateBookingCommand, CreateBookingResult> h) {
            this.handler = h;
        }

        void reset() {
            this.handler = cmd -> new CreateBookingResult(
                    "id", "s", "c", "svc", null, Instant.now(), null,
                    BigDecimal.ONE, null, "SCHEDULED", Instant.now(), false);
        }

        @Override
        public CreateBookingResult execute(CreateBookingCommand command) {
            return handler.apply(command);
        }
    }
}
