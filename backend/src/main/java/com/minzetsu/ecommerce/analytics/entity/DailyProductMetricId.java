package com.minzetsu.ecommerce.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DailyProductMetricId implements Serializable {

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "product_id", nullable = false)
    private Long productId;
}
