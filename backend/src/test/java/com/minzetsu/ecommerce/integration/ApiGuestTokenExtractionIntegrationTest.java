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
class ApiGuestTokenExtractionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void guestOrder_shouldFallbackToHeaderWhenQueryParamBlank() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1")
                        .param("accessToken", "   ")
                        .header("X-Guest-Access-Token", "invalid.token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid guest access token"));
    }

    @Test
    void guestOrder_shouldPrioritizeQueryParamWhenBothProvided() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1")
                        .param("accessToken", "invalid.token")
                        .header("X-Guest-Access-Token", "   "))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid guest access token"));
    }

    @Test
    void guestPayments_shouldRejectWhenBothTokenSourcesBlank() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1/payments")
                        .param("accessToken", " ")
                        .header("X-Guest-Access-Token", ""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Guest access token is required"));
    }

    @Test
    void guestMomoCreate_shouldFallbackToHeaderWhenQueryParamBlank() throws Exception {
        mockMvc.perform(post("/api/public/guest/orders/1/payments/momo/create")
                        .param("accessToken", "")
                        .header("X-Guest-Access-Token", "invalid.token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid guest access token"));
    }
}
