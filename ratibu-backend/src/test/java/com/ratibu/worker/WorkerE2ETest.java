package com.ratibu.worker;

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
class WorkerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createWorker_withoutMembership_returns201() throws Exception {
        String businessId = createBusiness();

        mockMvc.perform(post("/api/v1/businesses/{businessId}/workers", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"John Barber"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Barber"))
                .andExpect(jsonPath("$.businessId").value(businessId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.alreadyExisted").value(false));
    }

    @Test
    void createWorker_withMembership_returns201() throws Exception {
        String accountId = createAccount("worker-" + System.nanoTime() + "@salon.ke");
        String businessId = createBusiness();
        String membershipId = createMembership(businessId, accountId, "WORKER");

        mockMvc.perform(post("/api/v1/businesses/{businessId}/workers", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mary Stylist","membershipId":"%s"}
                                """.formatted(membershipId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mary Stylist"))
                .andExpect(jsonPath("$.membershipId").value(membershipId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createWorker_clientId_isIdempotent() throws Exception {
        String businessId = createBusiness();
        String workerId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

        String body = """
                {"workerId":"%s","name":"Idempotent Worker"}
                """.formatted(workerId);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/workers", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workerId").value(workerId));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/workers", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerId").value(workerId))
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }

    private String createAccount(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = node.path("accountId").asText(null);
        if (id == null) {
            id = node.path("id").asText(null);
        }
        assertNotNull(id);
        return id;
    }

    private String createBusiness() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Worker E2E Biz","currency":"KES"}
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

    private String createMembership(String businessId, String accountId, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses/{businessId}/memberships", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":"%s","role":"%s"}
                                """.formatted(accountId, role)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = node.path("membershipId").asText(null);
        if (id == null) {
            id = node.path("id").asText(null);
        }
        assertNotNull(id);
        return id;
    }
}
