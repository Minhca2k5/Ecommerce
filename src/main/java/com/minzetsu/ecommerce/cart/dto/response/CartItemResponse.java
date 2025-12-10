package com.minzetsu.ecommerce.cart.dto.response;

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
public class CartItemResponse extends BaseDTO {

    private Long cartId;

    private Long productId;
    private BigDecimal unitPriceSnapshot;
    private Integer quantity;
    private BigDecimal lineTotal;

    private String productName;
    private String productSlug;
    private String productSku;
    private BigDecimal productPrice;
    private String productCurrency;
    private String productStatus;
    private String url;
}
