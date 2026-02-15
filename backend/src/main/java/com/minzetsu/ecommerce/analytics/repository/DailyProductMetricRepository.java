package com.minzetsu.ecommerce.analytics.repository;

import com.minzetsu.ecommerce.analytics.entity.DailyProductMetric;
import com.minzetsu.ecommerce.analytics.entity.DailyProductMetricId;
import com.minzetsu.ecommerce.analytics.repository.projection.FunnelAggregateView;
import com.minzetsu.ecommerce.analytics.repository.projection.TopProductAggregateView;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyProductMetricRepository extends JpaRepository<DailyProductMetric, DailyProductMetricId> {

    @Modifying
    @Query("DELETE FROM DailyProductMetric d WHERE d.id.metricDate = :metricDate")
    int deleteByMetricDate(LocalDate metricDate);

    @Query("""
            SELECT
                COALESCE(SUM(d.views), 0) AS views,
                COALESCE(SUM(d.addToCart), 0) AS addToCart,
                COALESCE(SUM(d.orders), 0) AS orders
            FROM DailyProductMetric d
            WHERE d.id.metricDate BETWEEN :fromDate AND :toDate
            """)
    FunnelAggregateView aggregateFunnel(LocalDate fromDate, LocalDate toDate);

    @Query("""
            SELECT
                d.id.productId AS productId,
                p.name AS productName,
                COALESCE(SUM(d.views), 0) AS views,
                COALESCE(SUM(d.addToCart), 0) AS addToCart,
                COALESCE(SUM(d.orders), 0) AS orders,
                COALESCE(SUM(d.uniqueUsers), 0) AS uniqueUsers
            FROM DailyProductMetric d
            JOIN Product p ON p.id = d.id.productId
            WHERE d.id.metricDate BETWEEN :fromDate AND :toDate
            GROUP BY d.id.productId, p.name
            ORDER BY COALESCE(SUM(d.orders), 0) DESC, COALESCE(SUM(d.views), 0) DESC
            """)
    List<TopProductAggregateView> findTopProductsByRange(LocalDate fromDate, LocalDate toDate);

    @Query("SELECT MAX(d.id.metricDate) FROM DailyProductMetric d")
    Optional<LocalDate> findLatestMetricDate();
}
