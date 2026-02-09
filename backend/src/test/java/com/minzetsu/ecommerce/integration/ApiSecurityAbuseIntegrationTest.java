package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityAbuseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminEndpoint_shouldRejectInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldRejectInvalidAccessTokenQueryParam() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                        .param("access_token", "invalid.jwt.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicEndpoint_shouldBeRateLimitedForBurstFromSameClient() throws Exception {
        String forwardedFor = "203.0.113.10";
        boolean blocked = false;

        for (int i = 0; i < 70; i++) {
            int status = mockMvc.perform(get("/api/public/categories")
                            .header("X-Forwarded-For", forwardedFor))
                    .andReturn()
                    .getResponse()
                    .getStatus();
            if (status == 429) {
                blocked = true;
                break;
            }
        }

        assertThat(blocked).isTrue();
    }
}
