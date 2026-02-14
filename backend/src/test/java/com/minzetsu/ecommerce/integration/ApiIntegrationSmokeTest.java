package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpoint_shouldExposeStatusField() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void publicHome_shouldApplyPublicCachePolicy() throws Exception {
        mockMvc.perform(get("/api/public/home"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "public, max-age=60, must-revalidate"));
    }

    @Test
    void publicCategories_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void publicProducts_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk());
    }

    @Test
    void guestCartEndpoint_shouldAllowCartCreationWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/public/carts/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void publicProductByUnknownSlug_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/public/products/slug/definitely-missing-slug"))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicCategoryByUnknownSlug_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/public/categories/slug/definitely-missing-category"))
                .andExpect(status().isNotFound());
    }

    @Test
    void userProfileEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/users/me"))
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUsersUnknownId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/users/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void authLoginEndpoint_shouldReturnValidationErrorForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authRefreshEndpoint_shouldReturnValidationErrorForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guestPaymentsEndpoint_shouldRejectMissingAccessToken() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1/payments"))
                .andExpect(status().isForbidden());
    }
}
