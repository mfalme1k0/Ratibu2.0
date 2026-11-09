package com.ratibu.catalog;

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
class CatalogServiceE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createService_returns201() throws Exception {
        String businessId = createBusiness();

        mockMvc.perform(post("/api/v1/businesses/{businessId}/services", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Haircut","defaultPrice":500.00,"description":"Basic cut"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Haircut"))
                .andExpect(jsonPath("$.defaultPrice").value(500.00))
                .andExpect(jsonPath("$.description").value("Basic cut"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.alreadyExisted").value(false));
    }

    @Test
    void createService_clientId_isIdempotent() throws Exception {
        String businessId = createBusiness();
        String serviceId = "aaaaaaaa-bbbb-cccc-dddd-ffffffffffff";

        String body = """
                {"serviceId":"%s","name":"Manicure","defaultPrice":800}
                """.formatted(serviceId);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/services", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceId").value(serviceId));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/services", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceId").value(serviceId))
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }

    private String createBusiness() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Catalog E2E Biz","currency":"KES"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = node.path("businessId").asText(null);
        if (id == null) {
            id = node.path("id").asText(null);
        }
        assertNotNull(id);
        return id;
    }
}
