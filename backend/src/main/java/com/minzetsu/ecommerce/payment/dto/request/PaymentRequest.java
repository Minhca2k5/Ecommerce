package com.minzetsu.ecommerce.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @Builder.Default
    private String method = "COD";

    @Builder.Default
    private String status = "INITIATED";

    private String providerTxnId;
    private Long voucherId;
    private BigDecimal discountAmount;
}

