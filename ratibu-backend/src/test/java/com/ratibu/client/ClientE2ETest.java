package com.ratibu.client;

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
class ClientE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createClient_minimal_returns201() throws Exception {
        String businessId = createBusiness();

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jane Client"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane Client"))
                .andExpect(jsonPath("$.businessId").value(businessId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.alreadyExisted").value(false));
    }

    @Test
    void createClient_withPhone_returns201() throws Exception {
        String businessId = createBusiness();

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mary","phone":"+254700111222","email":"mary@salon.ke","notes":"regular"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("+254700111222"))
                .andExpect(jsonPath("$.email").value("mary@salon.ke"))
                .andExpect(jsonPath("$.notes").value("regular"));
    }

    @Test
    void createClient_clientId_isIdempotent() throws Exception {
        String businessId = createBusiness();
        String clientId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

        String body = """
                {"clientId":"%s","name":"Idempotent Client"}
                """.formatted(clientId);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(clientId));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }

    @Test
    void createClient_duplicatePhone_bothCreated() throws Exception {
        String businessId = createBusiness();

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"One","phone":"+254799999999"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/businesses/{businessId}/clients", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Two","phone":"+254799999999"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Two"));
    }

    private String createBusiness() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Client E2E Biz","currency":"KES"}
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
