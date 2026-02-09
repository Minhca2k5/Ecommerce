package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiRateLimitIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authRateLimit_shouldNotLeakAcrossDifferentClientIps() throws Exception {
        String exhaustedIp = "198.51.100.81";
        String freshIp = "198.51.100.82";

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", exhaustedIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", exhaustedIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", freshIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authRateLimit_shouldShareBucketAcrossAuthEndpointsForSameIp() throws Exception {
        String forwardedFor = "198.51.100.83";

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", forwardedFor)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        mockMvc.perform(post("/api/auth/refresh-token")
                        .header("X-Forwarded-For", forwardedFor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isTooManyRequests());
    }
}
