package com.minzetsu.ecommerce.payment.dto.response;

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
public class PaymentResponse extends BaseDTO {

    private Long orderId;

    private String method;
    private BigDecimal amount;
    private String status;
    private String providerTxnId;

    // bày vẽ hợp lý từ bảng orders
    private BigDecimal orderTotalAmount;
    private String orderCurrency;
    private String orderStatus;
}
