package com.minzetsu.ecommerce.analytics.repository;

import com.minzetsu.ecommerce.analytics.entity.DailyProductMetric;
import com.minzetsu.ecommerce.analytics.entity.DailyProductMetricId;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyProductMetricRepository extends JpaRepository<DailyProductMetric, DailyProductMetricId> {

    @Modifying
    @Query("DELETE FROM DailyProductMetric d WHERE d.id.metricDate = :metricDate")
    int deleteByMetricDate(LocalDate metricDate);
}
