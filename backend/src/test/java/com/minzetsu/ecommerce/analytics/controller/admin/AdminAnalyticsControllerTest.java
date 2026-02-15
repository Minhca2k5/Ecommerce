package com.minzetsu.ecommerce.analytics.controller.admin;

import com.minzetsu.ecommerce.analytics.dto.response.AdminFunnelAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.dto.response.AdminTopProductAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.service.AdminAnalyticsService;
import com.minzetsu.ecommerce.analytics.service.AnalyticsEtlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsControllerTest {

    @Mock
    private AdminAnalyticsService adminAnalyticsService;
    @Mock
    private AnalyticsEtlService analyticsEtlService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminAnalyticsController controller = new AdminAnalyticsController(adminAnalyticsService, analyticsEtlService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getFunnel_shouldReturnContractFields() throws Exception {
        when(adminAnalyticsService.getFunnel(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-02-15")))
                .thenReturn(AdminFunnelAnalyticsResponse.builder()
                        .from(LocalDate.parse("2026-02-01"))
                        .to(LocalDate.parse("2026-02-15"))
                        .views(100)
                        .addToCart(20)
                        .orders(5)
                        .viewToCartRate(new BigDecimal("0.2000"))
                        .cartToOrderRate(new BigDecimal("0.2500"))
                        .viewToOrderRate(new BigDecimal("0.0500"))
                        .build());

        mockMvc.perform(get("/api/admin/analytics/funnel")
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from[0]").value(2026))
                .andExpect(jsonPath("$.from[1]").value(2))
                .andExpect(jsonPath("$.from[2]").value(1))
                .andExpect(jsonPath("$.to[0]").value(2026))
                .andExpect(jsonPath("$.to[1]").value(2))
                .andExpect(jsonPath("$.to[2]").value(15))
                .andExpect(jsonPath("$.views").value(100))
                .andExpect(jsonPath("$.addToCart").value(20))
                .andExpect(jsonPath("$.orders").value(5))
                .andExpect(jsonPath("$.viewToOrderRate").value(0.0500));
    }

    @Test
    void getTopProducts_shouldReturnContractFields() throws Exception {
        when(adminAnalyticsService.getTopProducts(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-02-15"), 2))
                .thenReturn(List.of(
                        AdminTopProductAnalyticsResponse.builder()
                                .productId(11L)
                                .productName("Phone X")
                                .views(40)
                                .addToCart(10)
                                .orders(4)
                                .uniqueUsers(12)
                                .conversionRate(new BigDecimal("0.1000"))
                                .build(),
                        AdminTopProductAnalyticsResponse.builder()
                                .productId(12L)
                                .productName("Tablet Y")
                                .views(50)
                                .addToCart(7)
                                .orders(3)
                                .uniqueUsers(15)
                                .conversionRate(new BigDecimal("0.0600"))
                                .build()
                ));

        mockMvc.perform(get("/api/admin/analytics/top-products")
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-15")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(11))
                .andExpect(jsonPath("$[0].productName").value("Phone X"))
                .andExpect(jsonPath("$[0].conversionRate").value(0.1000))
                .andExpect(jsonPath("$[1].productId").value(12));
    }

    @Test
    void runEtlForDate_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/admin/analytics/etl/run")
                        .param("date", "2026-02-15"))
                .andExpect(status().isOk())
                .andExpect(content().string("Analytics ETL completed for date 2026-02-15"));
    }
}
