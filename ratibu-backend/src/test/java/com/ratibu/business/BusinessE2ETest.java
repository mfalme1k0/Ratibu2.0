package com.ratibu.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end vertical slice: HTTP → controller → application → domain →
 * JPA adapter → H2 (Flyway schema). No mocks of application or persistence.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
class BusinessE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBusiness_persistsAndReturns201() throws Exception {
        String body = """
                {"name":"John's Barbershop","currency":"KES"}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John's Barbershop"))
                .andExpect(jsonPath("$.currency").value("KES"))
                .andExpect(jsonPath("$.alreadyExisted").value(false))
                .andExpect(jsonPath("$.businessId").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertNotNull(json.get("businessId").asText());
    }

    @Test
    void createBusiness_withClientId_isIdempotentAcrossRequests() throws Exception {
        String clientId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        String body = """
                {"businessId":"%s","name":"Mary's Salon","currency":"USD"}
                """.formatted(clientId);

        mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessId").value(clientId))
                .andExpect(jsonPath("$.alreadyExisted").value(false));

        mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessId").value(clientId))
                .andExpect(jsonPath("$.name").value("Mary's Salon"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }

    @Test
    void createBusiness_rejectsBlankName_with400() throws Exception {
        String body = """
                {"name":"   ","currency":"KES"}
                """;

        mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void createBusiness_rejectsUnsupportedCurrency_with400() throws Exception {
        String body = """
                {"name":"Shop","currency":"JPY"}
                """;

        mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invariant Violation"));
    }
}
