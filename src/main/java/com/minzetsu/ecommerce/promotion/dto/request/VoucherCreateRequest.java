package com.minzetsu.ecommerce.promotion.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherCreateRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderTotal;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private Integer usageLimitGlobal;
    private Integer usageLimitUser;

    @Builder.Default
    private String status = "ACTIVE";
}
