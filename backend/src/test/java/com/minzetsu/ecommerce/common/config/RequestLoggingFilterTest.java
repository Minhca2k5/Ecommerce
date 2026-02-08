package com.minzetsu.ecommerce.common.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @AfterEach
    void cleanup() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldProceedAndKeepResponseStatus() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        MDC.put(RequestIdFilter.MDC_REQUEST_ID_KEY, "req-1");

        FilterChain chain = (req, res) -> ((MockHttpServletResponse) res).setStatus(204);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    void doFilter_shouldHandleAuthenticatedContextWithoutError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/user/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("demo-user", "n/a", java.util.List.of())
        );

        FilterChain chain = (req, res) -> ((MockHttpServletResponse) res).setStatus(201);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(201);
    }
}
