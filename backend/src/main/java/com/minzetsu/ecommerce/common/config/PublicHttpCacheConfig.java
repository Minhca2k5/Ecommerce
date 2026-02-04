package com.minzetsu.ecommerce.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.io.IOException;

@Configuration
public class PublicHttpCacheConfig {

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> publicEtagFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ShallowEtagHeaderFilter());
        registration.addUrlPatterns("/api/public/*");
        registration.setName("publicEtagFilter");
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 20);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> publicCacheControlFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {
                filterChain.doFilter(request, response);

                if (!"GET".equalsIgnoreCase(request.getMethod()) || response.isCommitted()) {
                    return;
                }
                if (response.getHeader("Cache-Control") != null) {
                    return;
                }

                String uri = request.getRequestURI();
                if (uri.equals("/api/public/home") || uri.startsWith("/api/public/banners")) {
                    response.setHeader("Cache-Control", "public, max-age=60, must-revalidate");
                    return;
                }
                if (uri.matches("^/api/public/products/[^/]+/reviews$")) {
                    response.setHeader("Cache-Control", "no-store");
                    return;
                }
                if (uri.matches("^/api/public/products/[^/]+$") || uri.matches("^/api/public/products/slug/[^/]+$")) {
                    response.setHeader("Cache-Control", "no-cache, must-revalidate");
                    return;
                }
                if (uri.startsWith("/api/public/products") || uri.startsWith("/api/public/categories")) {
                    response.setHeader("Cache-Control", "public, max-age=120, must-revalidate");
                }
            }
        });
        registration.addUrlPatterns("/api/public/*");
        registration.setName("publicCacheControlFilter");
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        return registration;
    }
}
