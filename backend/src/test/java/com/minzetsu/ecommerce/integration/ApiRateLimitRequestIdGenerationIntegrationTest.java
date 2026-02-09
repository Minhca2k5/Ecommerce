package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class ApiRateLimitRequestIdGenerationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rateLimitResponse_shouldGenerateRequestIdWhenMissing() throws Exception {
        String forwardedFor = "198.51.100.101";
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
        String requestId = blocked.getResponse().getHeader("X-Request-Id");
        assertThat(requestId).isNotBlank();
        assertThat(Pattern.compile("^[0-9a-fA-F-]{36}$").matcher(requestId).matches()).isTrue();
        assertThat(blocked.getResponse().getContentAsString()).contains("Too many requests");
    }
}
