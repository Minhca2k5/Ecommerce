package com.minzetsu.ecommerce.cart.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse extends BaseDTO {

    private Long userId;
    private String username;
    private String fullName;
    private String guestId;

    private List<CartItemResponse> items;

    private Integer itemCount;
    private Integer totalQuantity;
    private BigDecimal itemsSubtotal;
    private BigDecimal discount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String currency;
}
