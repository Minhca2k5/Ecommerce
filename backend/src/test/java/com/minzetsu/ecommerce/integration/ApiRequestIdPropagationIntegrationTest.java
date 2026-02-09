package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiRequestIdPropagationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_shouldEchoProvidedRequestId() throws Exception {
        mockMvc.perform(get("/api/public/products")
                        .header("X-Request-Id", "rid-public-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "rid-public-1"));
    }

    @Test
    void authValidationEndpoint_shouldEchoProvidedRequestId() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Request-Id", "rid-auth-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Request-Id", "rid-auth-1"));
    }

    @Test
    void forbiddenEndpoint_shouldEchoProvidedRequestId() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                        .header("X-Request-Id", "rid-forbidden-1"))
                .andExpect(status().isForbidden())
                .andExpect(header().string("X-Request-Id", "rid-forbidden-1"));
    }

    @Test
    void serverErrorEndpoint_shouldEchoProvidedRequestId() throws Exception {
        mockMvc.perform(get("/actuator/info")
                        .header("X-Request-Id", "rid-error-1"))
                .andExpect(status().is5xxServerError())
                .andExpect(header().string("X-Request-Id", "rid-error-1"));
    }
}
