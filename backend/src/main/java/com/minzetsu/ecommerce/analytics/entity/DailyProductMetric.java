package com.minzetsu.ecommerce.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_product_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyProductMetric {

    @EmbeddedId
    private DailyProductMetricId id;

    @Column(name = "views", nullable = false)
    private long views;

    @Column(name = "add_to_cart", nullable = false)
    private long addToCart;

    @Column(name = "orders", nullable = false)
    private long orders;

    @Column(name = "unique_users", nullable = false)
    private long uniqueUsers;

    @Column(name = "conversion_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal conversionRate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static BigDecimal computeConversionRate(long views, long orders) {
        if (views <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(orders)
                .divide(BigDecimal.valueOf(views), 4, RoundingMode.HALF_UP);
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
