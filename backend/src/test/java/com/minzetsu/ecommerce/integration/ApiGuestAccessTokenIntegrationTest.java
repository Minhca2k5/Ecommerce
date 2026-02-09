package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiGuestAccessTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void guestOrder_shouldRejectMissingAccessToken() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Guest access token is required"));
    }

    @Test
    void guestOrder_shouldRejectMalformedAccessToken() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1")
                        .param("accessToken", "invalid.token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid guest access token"));
    }

    @Test
    void guestPayments_shouldRejectMissingAccessToken() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1/payments"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Guest access token is required"));
    }

    @Test
    void guestMomoCreate_shouldRejectMalformedAccessTokenHeader() throws Exception {
        mockMvc.perform(post("/api/public/guest/orders/1/payments/momo/create")
                        .header("X-Guest-Access-Token", "invalid.token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid guest access token"));
    }
}
