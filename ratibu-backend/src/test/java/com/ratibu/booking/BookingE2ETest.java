package com.ratibu.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingE2ETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void createBooking_endToEnd_returns201() throws Exception {
        String businessId = createBusiness();
        String shopId = createShop(businessId);
        String clientId = createClient(businessId);
        String serviceId = createService(businessId);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shopId":"%s",
                                  "clientId":"%s",
                                  "serviceId":"%s",
                                  "scheduledAt":"2026-12-15T09:00:00Z",
                                  "durationMinutes":45,
                                  "expectedPrice":500.00
                                }
                                """.formatted(shopId, clientId, serviceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.shopId").value(shopId))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.serviceId").value(serviceId))
                .andExpect(jsonPath("$.expectedPrice").value(500.00));
    }

    private String createBusiness() throws Exception {
        return idFrom(postJson("/api/v1/businesses",
                """
                {"name":"Booking E2E","currency":"KES"}
                """), "businessId");
    }

    private String createShop(String businessId) throws Exception {
        return idFrom(postJson("/api/v1/businesses/" + businessId + "/shops",
                """
                {"name":"Westlands"}
                """), "shopId");
    }

    private String createClient(String businessId) throws Exception {
        return idFrom(postJson("/api/v1/businesses/" + businessId + "/clients",
                """
                {"name":"Jane"}
                """), "clientId");
    }

    private String createService(String businessId) throws Exception {
        return idFrom(postJson("/api/v1/businesses/" + businessId + "/services",
                """
                {"name":"Haircut","defaultPrice":500.00}
                """), "serviceId");
    }

    private MvcResult postJson(String path, String body) throws Exception {
        return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private String idFrom(MvcResult result, String field) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = node.path(field).asText(null);
        if (id == null) {
            id = node.path("id").asText(null);
        }
        assertNotNull(id);
        return id;
    }
}
