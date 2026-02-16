package com.minzetsu.ecommerce.analytics.controller.admin;

import com.minzetsu.ecommerce.analytics.dto.response.AdminFunnelAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.dto.response.AdminTopProductAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.service.AdminAnalyticsService;
import com.minzetsu.ecommerce.analytics.service.AnalyticsEtlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Analytics", description = "Analytics serving APIs for admin")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;
    private final AnalyticsEtlService analyticsEtlService;

    @Operation(summary = "Get funnel analytics")
    @GetMapping("/funnel")
    public ResponseEntity<AdminFunnelAnalyticsResponse> getFunnel(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(adminAnalyticsService.getFunnel(from, to));
    }

    @Operation(summary = "Get top products by conversion")
    @GetMapping("/top-products")
    public ResponseEntity<List<AdminTopProductAnalyticsResponse>> getTopProducts(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(adminAnalyticsService.getTopProducts(from, to, limit));
    }

    @Operation(summary = "Run analytics ETL manually for a date")
    @PostMapping("/etl/run")
    @CacheEvict(cacheNames = "analyticsAdmin", allEntries = true)
    public ResponseEntity<String> runEtlForDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        analyticsEtlService.runEtlForDate(date);
        return ResponseEntity.ok("Analytics ETL completed for date " + date);
    }
}
