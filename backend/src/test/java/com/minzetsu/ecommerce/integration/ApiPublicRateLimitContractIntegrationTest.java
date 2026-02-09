package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiPublicRateLimitContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpointRateLimit_shouldReturn429WithStandardPayload() throws Exception {
        String forwardedFor = "198.51.100.121";

        for (int i = 0; i < 60; i++) {
            mockMvc.perform(get("/api/public/categories")
                            .header("X-Forwarded-For", forwardedFor))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/public/categories")
                        .header("X-Forwarded-For", forwardedFor))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.message").value("Too many requests"));
    }

    @Test
    void publicEndpointRateLimit_shouldPreserveProvidedRequestId() throws Exception {
        String forwardedFor = "198.51.100.122";
        String requestId = "rid-public-rate-limit-1";

        for (int i = 0; i < 60; i++) {
            mockMvc.perform(get("/api/public/categories")
                            .header("X-Forwarded-For", forwardedFor)
                            .header("X-Request-Id", requestId))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/public/categories")
                        .header("X-Forwarded-For", forwardedFor)
                        .header("X-Request-Id", requestId))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-Request-Id", requestId))
                .andExpect(jsonPath("$.message").value("Too many requests"));
    }
}
