package com.minzetsu.ecommerce.analytics.service;

import com.minzetsu.ecommerce.analytics.dto.response.AdminFunnelAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.dto.response.AdminTopProductAnalyticsResponse;
import java.time.LocalDate;
import java.util.List;

public interface AdminAnalyticsService {
    AdminFunnelAnalyticsResponse getFunnel(LocalDate from, LocalDate to);
    List<AdminTopProductAnalyticsResponse> getTopProducts(LocalDate from, LocalDate to, int limit);
}
