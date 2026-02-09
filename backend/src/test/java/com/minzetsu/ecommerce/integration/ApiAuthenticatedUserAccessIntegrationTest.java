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

@SpringBootTest
@AutoConfigureMockMvc
class ApiAuthenticatedUserAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    void userProfile_shouldNotBeBlockedBySecurityLayer() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }

    @Test
    @WithMockUser(roles = "USER")
    void userOrders_shouldNotBeBlockedBySecurityLayer() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me/orders"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }

    @Test
    @WithMockUser(roles = "USER")
    void userAddresses_shouldNotBeBlockedBySecurityLayer() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me/addresses"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }

    @Test
    @WithMockUser(roles = "USER")
    void userNotifications_shouldNotBeBlockedBySecurityLayer() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me/notifications"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }

    @Test
    @WithMockUser(roles = "USER")
    void userWishlist_shouldNotBeBlockedBySecurityLayer() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me/wishlists"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }
}
