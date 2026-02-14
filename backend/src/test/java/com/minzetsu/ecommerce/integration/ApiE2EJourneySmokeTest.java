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
class ApiE2EJourneySmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousJourney_shouldBrowsePublicAndCreateGuestCart() throws Exception {
        mockMvc.perform(get("/api/public/home")).andExpect(status().isOk());
        mockMvc.perform(get("/api/public/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/api/public/products")).andExpect(status().isOk());

        mockMvc.perform(post("/api/public/carts/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void anonymousJourney_shouldBeBlockedOnProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/roles")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userJourney_shouldAccessUserScopesButNotAdminScopes() throws Exception {
        MvcResult userOrders = mockMvc.perform(get("/api/users/me/orders")).andReturn();
        assertThat(userOrders.getResponse().getStatus()).isIn(200, 400, 404);

        mockMvc.perform(get("/api/admin/roles")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminJourney_shouldAccessAdminScopes() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk());
    }
}

