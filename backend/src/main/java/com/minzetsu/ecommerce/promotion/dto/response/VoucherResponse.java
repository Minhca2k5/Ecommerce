package com.minzetsu.ecommerce.promotion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoucherResponse extends BaseDTO {

    private String code;
    private String name;
    private String description;

    private String discountType;

    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderTotal;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
