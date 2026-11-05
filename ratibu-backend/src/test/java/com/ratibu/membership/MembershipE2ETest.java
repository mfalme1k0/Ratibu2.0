package com.ratibu.membership;

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
class MembershipE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMembership_owner_returns201() throws Exception {
        String accountId = createAccount("owner-" + System.nanoTime() + "@beauty.empire");
        String businessId = createBusiness();

        mockMvc.perform(post("/api/v1/businesses/{businessId}/memberships", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":"%s","role":"OWNER"}
                                """.formatted(accountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.businessId").value(businessId))
                .andExpect(jsonPath("$.alreadyExisted").value(false));
    }

    @Test
    void createMembership_samePair_isIdempotent() throws Exception {
        String accountId = createAccount("mgr-" + System.nanoTime() + "@beauty.empire");
        String businessId = createBusiness();
        String body = """
                {"accountId":"%s","role":"MANAGER"}
                """.formatted(accountId);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/memberships", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/businesses/{businessId}/memberships", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }

    @Test
    void createMembership_unknownAccount_returns404() throws Exception {
        String businessId = createBusiness();

        mockMvc.perform(post("/api/v1/businesses/{businessId}/memberships", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":"dddddddd-dddd-dddd-dddd-dddddddddddd","role":"WORKER"}
                                """))
                .andExpect(status().isNotFound());
    }

    private String createAccount(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","displayName":"User"}
                                """.formatted(email)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = json.get("accountId").asText();
        assertNotNull(id);
        return id;
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
