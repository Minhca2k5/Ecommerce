package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class ApiTokenMisuseStabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void guestOrderEndpoint_shouldConsistentlyRejectMalformedTokenWithoutServerError() throws Exception {
        for (int i = 0; i < 20; i++) {
            MvcResult result = mockMvc.perform(get("/api/public/guest/orders/1")
                            .param("accessToken", "invalid.token." + i))
                    .andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(403);
        }
    }

    @Test
    void guestPaymentsEndpoint_shouldConsistentlyRejectMalformedTokenWithoutServerError() throws Exception {
        for (int i = 0; i < 20; i++) {
            MvcResult result = mockMvc.perform(get("/api/public/guest/orders/1/payments")
                            .header("X-Guest-Access-Token", "invalid.token." + i))
                    .andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(403);
        }
    }

    @Test
    void guestMomoCreateEndpoint_shouldConsistentlyRejectMalformedTokenWithoutServerError() throws Exception {
        for (int i = 0; i < 20; i++) {
            MvcResult result = mockMvc.perform(post("/api/public/guest/orders/1/payments/momo/create")
                            .param("accessToken", "invalid.token." + i))
                    .andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(403);
        }
    }
}
