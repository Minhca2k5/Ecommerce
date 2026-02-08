package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        MvcResult result = mockMvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("$.status").exists())
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 503);
    }

    @Test
    void publicHome_shouldApplyPublicCachePolicy() throws Exception {
        mockMvc.perform(get("/api/public/home"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "public, max-age=60, must-revalidate"));
    }

    @Test
    void guestOrderEndpoint_shouldRejectMissingAccessToken() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoint_shouldRejectUserRole() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoint_shouldAllowAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk());
    }
}
