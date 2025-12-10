package com.minzetsu.ecommerce.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    private Long userId;
    private Long voucherId;
    private BigDecimal shippingFee;

    @NotNull(message = "Cart ID is required")
    private Long cartId;

    @NotNull(message = "Address snapshot ID is required")
    private Long addressIdSnapshot;

    @Builder.Default
    private String currency = "VND";

    @Builder.Default
    private String status = "PENDING";
}
