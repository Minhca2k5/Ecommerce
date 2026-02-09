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
class ApiGuestPaymentTokenMisuseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listPayments_shouldRejectMissingTokenWhenHeaderBlank() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1/payments")
                        .header("X-Guest-Access-Token", "   "))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Guest access token is required"));
    }

    @Test
    void listPayments_shouldRejectMalformedTokenInQueryParam() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1/payments")
                        .param("accessToken", "invalid.token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid guest access token"));
    }

    @Test
    void createMomo_shouldRejectMissingTokenWhenHeaderBlank() throws Exception {
        mockMvc.perform(post("/api/public/guest/orders/1/payments/momo/create")
                        .header("X-Guest-Access-Token", ""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Guest access token is required"));
    }

    @Test
    void createMomo_shouldRejectMalformedTokenInQueryParam() throws Exception {
        mockMvc.perform(post("/api/public/guest/orders/1/payments/momo/create")
                        .param("accessToken", "invalid.token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid guest access token"));
    }
}
