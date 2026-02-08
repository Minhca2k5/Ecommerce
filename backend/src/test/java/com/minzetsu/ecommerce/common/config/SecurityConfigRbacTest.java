package com.minzetsu.ecommerce.common.config;

import com.minzetsu.ecommerce.auth.controller.AuthController;
import com.minzetsu.ecommerce.auth.dto.request.LoginRequest;
import com.minzetsu.ecommerce.auth.service.AuthService;
import com.minzetsu.ecommerce.user.controller.admin.AdminRoleController;
import com.minzetsu.ecommerce.user.dto.response.RoleResponse;
import com.minzetsu.ecommerce.user.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, AdminRoleController.class})
@Import(SecurityConfig.class)
class SecurityConfigRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RequestIdFilter requestIdFilter;

    @MockBean
    private RequestLoggingFilter requestLoggingFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUpFilters() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(requestIdFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(requestLoggingFilter).doFilter(any(), any(), any());
    }

    @Test
    void authEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("demo");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_shouldRequireAuthentication() throws Exception {
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
        when(roleService.getAllRoleResponses()).thenReturn(List.of(RoleResponse.builder().name("ADMIN").build()));

        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_preflightShouldPassWithoutAuthentication() throws Exception {
        mockMvc.perform(options("/api/admin/roles")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }
}
