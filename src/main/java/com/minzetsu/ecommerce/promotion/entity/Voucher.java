package com.minzetsu.ecommerce.promotion.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "vouchers",
        uniqueConstraints = @UniqueConstraint(name = "uq_vouchers_code", columnNames = "code")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20, nullable = false)
    private VoucherDiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderTotal;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    private Integer usageLimitGlobal;

    private Integer usageLimitUser;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private VoucherStatus status;
}
