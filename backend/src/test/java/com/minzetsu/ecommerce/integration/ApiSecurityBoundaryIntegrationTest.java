package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityBoundaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicMomoIpn_shouldRejectUnsignedAnonymousRequest() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/public/payments/momo/ipn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void guestCheckout_shouldNotBeBlockedByAuthenticationLayer() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/public/checkout/guest/guest-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }

    @Test
    void adminSearchReindex_shouldRejectAnonymous() throws Exception {
        mockMvc.perform(post("/api/admin/search/reindex"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminSearchReindex_shouldRejectUserRole() throws Exception {
        mockMvc.perform(post("/api/admin/search/reindex"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminSearchReindex_shouldAllowAdminRole() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/search/reindex"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 400, 404, 500);
    }

    @Test
    @WithMockUser(roles = "USER")
    void userOrderEndpoint_shouldAllowAuthenticatedUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me/orders"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }
}


