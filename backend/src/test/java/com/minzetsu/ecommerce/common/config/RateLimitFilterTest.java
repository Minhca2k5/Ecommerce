package com.minzetsu.ecommerce.common.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter blockedCounter;

    private RateLimitProperties properties;
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setBypass(List.of("/docs/**"));
        properties.setPublicRule(new RateLimitProperties.Rule(1, 1, Duration.ofMinutes(1)));
        properties.setAuthRule(new RateLimitProperties.Rule(1, 1, Duration.ofMinutes(1)));
        properties.setUserRule(new RateLimitProperties.Rule(1, 1, Duration.ofMinutes(1)));
        properties.setAdminRule(new RateLimitProperties.Rule(1, 1, Duration.ofMinutes(1)));

        rateLimitFilter = new RateLimitFilter(properties, meterRegistry);
    }

    @Test
    void doFilter_shouldBypassConfiguredPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/docs/openapi");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        rateLimitFilter.doFilter(request, response, chain);
        rateLimitFilter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void doFilter_shouldBypassWhenK6UserAgentBypassEnabled() throws Exception {
        properties.setAllowK6UserAgentBypass(true);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/products");
        request.addHeader("User-Agent", "k6-load-test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        rateLimitFilter.doFilter(request, response, chain);
        rateLimitFilter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void doFilter_shouldAllowFirstRequestWithinLimit() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        rateLimitFilter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void doFilter_shouldBlockWhenLimitExceeded() throws Exception {
        when(meterRegistry.counter("rate_limit.blocked")).thenReturn(blockedCounter);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/products");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        rateLimitFilter.doFilter(request, firstResponse, chain);
        rateLimitFilter.doFilter(request, secondResponse, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(secondResponse.getContentAsString()).contains("Too many requests");
        verify(blockedCounter).increment();
    }

    @Test
    void doFilter_shouldUseForwardedIpForRateLimitBucket() throws Exception {
        when(meterRegistry.counter("rate_limit.blocked")).thenReturn(blockedCounter);

        MockHttpServletRequest requestA = new MockHttpServletRequest("GET", "/api/public/products");
        requestA.addHeader("X-Forwarded-For", "1.2.3.4");
        MockHttpServletRequest requestB = new MockHttpServletRequest("GET", "/api/public/products");
        requestB.addHeader("X-Forwarded-For", "1.2.3.4");

        MockHttpServletResponse responseA = new MockHttpServletResponse();
        MockHttpServletResponse responseB = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        rateLimitFilter.doFilter(requestA, responseA, chain);
        rateLimitFilter.doFilter(requestB, responseB, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(responseB.getStatus()).isEqualTo(429);
        verify(blockedCounter).increment();
    }
}
