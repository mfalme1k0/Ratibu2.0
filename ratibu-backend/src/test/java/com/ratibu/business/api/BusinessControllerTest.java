package com.ratibu.business.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratibu.business.application.CreateBusinessCommand;
import com.ratibu.business.application.CreateBusinessResult;
import com.ratibu.business.application.CreateBusinessService;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.web.ApiExceptionHandler;
import com.ratibu.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BusinessController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateBusinessService createBusinessService;

    @Test
    void create_returns201_whenNewBusiness() throws Exception {
        when(createBusinessService.execute(any(CreateBusinessCommand.class)))
                .thenReturn(new CreateBusinessResult(
                        "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                        "John's Barbershop",
                        "KES",
                        Instant.parse("2026-11-02T12:00:00Z"),
                        false));

        String body = objectMapper.writeValueAsString(
                new CreateBusinessRequest(null, "John's Barbershop", "KES"));

        mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessId").value("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
                .andExpect(jsonPath("$.name").value("John's Barbershop"))
                .andExpect(jsonPath("$.currency").value("KES"))
                .andExpect(jsonPath("$.alreadyExisted").value(false));
    }

    @Test
    void create_returns200_whenIdempotentReplay() throws Exception {
        when(createBusinessService.execute(any(CreateBusinessCommand.class)))
                .thenReturn(new CreateBusinessResult(
                        "11111111-2222-3333-4444-555555555555",
                        "Mary's Salon",
                        "USD",
                        Instant.parse("2026-11-01T10:00:00Z"),
                        true));

        String body = objectMapper.writeValueAsString(
                new CreateBusinessRequest(
                        "11111111-2222-3333-4444-555555555555",
                        "Mary's Salon",
                        "USD"));

        mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }

    @Test
    void create_returns400_whenNameBlank() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateBusinessRequest(null, "  ", "KES"));

        mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void create_returns400_whenDomainInvariantFails() throws Exception {
        when(createBusinessService.execute(any(CreateBusinessCommand.class)))
                .thenThrow(new InvariantViolationException("Currency code is not supported in MVP: JPY"));

        String body = objectMapper.writeValueAsString(
                new CreateBusinessRequest(null, "Shop", "JPY"));

        mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invariant Violation"))
                .andExpect(jsonPath("$.message").value("Currency code is not supported in MVP: JPY"));
    }
}
