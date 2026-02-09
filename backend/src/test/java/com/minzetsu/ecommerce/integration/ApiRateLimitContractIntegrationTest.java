package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiRateLimitContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authLoginRateLimit_shouldReturnStandard429Payload() throws Exception {
        String forwardedFor = "203.0.113.71";
        MvcResult blocked = null;

        for (int i = 0; i < 20; i++) {
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", forwardedFor)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andReturn();
            if (result.getResponse().getStatus() == 429) {
                blocked = result;
                break;
            }
        }

        assertThat(blocked).isNotNull();
        String contentType = blocked.getResponse().getContentType();
        assertThat(contentType).isNotNull();
        assertThat(contentType).contains("application/json");
        assertThat(blocked.getResponse().getContentAsString()).contains("Too many requests");
    }

    @Test
    void authLoginRateLimit_shouldKeepRequestIdHeader() throws Exception {
        String forwardedFor = "203.0.113.72";
        String providedRequestId = "rid-rate-limit-1";

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", forwardedFor)
                            .header("X-Request-Id", providedRequestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", forwardedFor)
                        .header("X-Request-Id", providedRequestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-Request-Id", providedRequestId))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Too many requests"));
    }
}
