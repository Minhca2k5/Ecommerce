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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityBoundaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicMomoIpn_shouldRejectUnsignedAnonymousRequest() throws Exception {
        mockMvc.perform(post("/api/public/payments/momo/ipn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
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
    void adminSearchReindex_shouldAllowAdminRole_withoutServerError() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/search/reindex"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isLessThan(500);
    }
}

