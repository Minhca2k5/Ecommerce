package com.minzetsu.ecommerce.promotion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoucherUseResponse extends BaseDTO {

    private Long voucherId;
    private Long userId;
    private Long orderId;

    private BigDecimal discountAmount;
}
