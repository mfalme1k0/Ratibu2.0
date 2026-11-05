package com.ratibu.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AccountE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAccount_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"owner@beauty.empire","displayName":"Owner"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("owner@beauty.empire"))
                .andExpect(jsonPath("$.displayName").value("Owner"))
                .andExpect(jsonPath("$.alreadyExisted").value(false))
                .andExpect(jsonPath("$.accountId").isNotEmpty());
    }

    @Test
    void createAccount_sameEmail_isIdempotent() throws Exception {
        String body = """
                {"email":"dup@example.com","displayName":"Dup"}
                """;

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyExisted").value(true));
    }
}
