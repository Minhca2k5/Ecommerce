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
    void publicCategories_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void publicBanners_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/banners"))
                .andExpect(status().isOk());
    }

    @Test
    void publicRealtime_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/realtime/new-products"))
                .andExpect(status().isOk());
    }

    @Test
    void guestOrderEndpoint_shouldRejectMissingAccessToken() throws Exception {
        mockMvc.perform(get("/api/public/guest/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userProfileEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userOrdersEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/users/me/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userAddressEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/users/me/addresses/default"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userNotificationsEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/users/me/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userSearchLogsEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/users/me/search-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userWishlistsEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/users/me/wishlists"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userRecentViewsEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/users/me/recent-views"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userRealtimeEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/users/me/realtime/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUsersEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminProductsEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/admin/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminOrdersEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/admin/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAuditLogsEndpoint_shouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminUsersEndpoint_shouldRejectUserRole() throws Exception {
        mockMvc.perform(get("/api/admin/users/1"))
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
    void adminUsersEndpoint_shouldAllowAdminRole() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/users/1"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 404);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminOrdersEndpoint_shouldAllowAdminRole() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/orders/1"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 404);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminProductsEndpoint_shouldAllowAdminRole() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/products/1"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 404);
    }
}
