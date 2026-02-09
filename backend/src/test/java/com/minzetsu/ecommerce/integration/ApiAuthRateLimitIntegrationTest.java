package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class ApiAuthRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginEndpoint_shouldBeRateLimitedForBurstFromSameClient() throws Exception {
        boolean blocked = false;
        String forwardedFor = "198.51.100.71";

        for (int i = 0; i < 20; i++) {
            int status = mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", forwardedFor)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
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

    @Test
    void refreshTokenEndpoint_shouldBeRateLimitedForBurstFromSameClient() throws Exception {
        boolean blocked = false;
        String forwardedFor = "198.51.100.72";

        for (int i = 0; i < 20; i++) {
            int status = mockMvc.perform(post("/api/auth/refresh-token")
                            .header("X-Forwarded-For", forwardedFor)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
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
