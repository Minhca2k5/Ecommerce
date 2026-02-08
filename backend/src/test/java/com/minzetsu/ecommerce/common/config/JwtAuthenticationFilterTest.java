package com.minzetsu.ecommerce.common.config;

import com.minzetsu.ecommerce.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldPassThroughWhenNoTokenProvided() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        filter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).extractUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void doFilter_shouldAuthenticateWhenBearerTokenIsValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        request.addHeader("Authorization", "Bearer token-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        UserDetails userDetails = new User("alice", "n/a", List.of());
        when(jwtService.extractUsername("token-123")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
        when(jwtService.isTokenValid("token-123", userDetails)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("alice");
    }

    @Test
    void doFilter_shouldAuthenticateWhenBearerPrefixIsLowercase() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        request.addHeader("Authorization", "bearer token-lower");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        UserDetails userDetails = new User("charlie", "n/a", List.of());
        when(jwtService.extractUsername("token-lower")).thenReturn("charlie");
        when(userDetailsService.loadUserByUsername("charlie")).thenReturn(userDetails);
        when(jwtService.isTokenValid("token-lower", userDetails)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("charlie");
    }

    @Test
    void doFilter_shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        request.addHeader("Authorization", "Bearer token-invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        UserDetails userDetails = new User("alice", "n/a", List.of());
        when(jwtService.extractUsername("token-invalid")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
        when(jwtService.isTokenValid("token-invalid", userDetails)).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_shouldUseAccessTokenQueryParamWhenHeaderNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        request.addHeader("Authorization", "Token abc");
        request.setParameter("access_token", "query-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        UserDetails userDetails = new User("bob", "n/a", List.of());
        when(jwtService.extractUsername("query-token")).thenReturn("bob");
        when(userDetailsService.loadUserByUsername("bob")).thenReturn(userDetails);
        when(jwtService.isTokenValid("query-token", userDetails)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("bob");
    }

    @Test
    void doFilter_shouldPassThroughWhenTokenParsingFails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        when(jwtService.extractUsername("bad-token")).thenThrow(new RuntimeException("invalid"));

        filter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void doFilter_shouldNotReloadUserWhenContextAlreadyAuthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        request.addHeader("Authorization", "Bearer token-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing-user", "n/a", List.of())
        );
        when(jwtService.extractUsername("token-123")).thenReturn("alice");

        filter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("existing-user");
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
    }
}
