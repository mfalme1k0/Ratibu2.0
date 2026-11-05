package com.ratibu.shop;

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

/**
 * Full stack: create Business then Shop (HTTP → app → domain → JPA → H2).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ShopE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createShop_underBusiness_returns201() throws Exception {
        String businessId = createBusiness();

        mockMvc.perform(post("/api/v1/businesses/{businessId}/shops", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Westlands"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Westlands"))
                .andExpect(jsonPath("$.businessId").value(businessId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.alreadyExisted").value(false))
                .andExpect(jsonPath("$.shopId").isNotEmpty());
    }

    @Test
    void createShop_idempotentWithClientId() throws Exception {
        String businessId = createBusiness();
        String shopId = "cccccccc-cccc-cccc-cccc-cccccccccccc";
        String body = """
                {"shopId":"%s","name":"Kilimani"}
                """.formatted(shopId);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/shops", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shopId").value(shopId));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/shops", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyExisted").value(true))
                .andExpect(jsonPath("$.shopId").value(shopId));
    }

    @Test
    void createShop_unknownBusiness_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/businesses/{businessId}/shops",
                        "dddddddd-dddd-dddd-dddd-dddddddddddd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Orphan"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    private String createBusiness() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Beauty Empire","currency":"KES"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = json.get("businessId").asText();
        assertNotNull(id);
        return id;
    }
}
