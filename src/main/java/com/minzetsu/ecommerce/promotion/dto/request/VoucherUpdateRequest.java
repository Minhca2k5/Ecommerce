package com.minzetsu.ecommerce.promotion.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherUpdateRequest {

    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderTotal;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Integer usageLimitGlobal;
    private Integer usageLimitUser;

    private String status;
}
