package com.minzetsu.ecommerce.activity.dto.response;

import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponse extends BaseDTO {

    private Long userId;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productSku;
    private BigDecimal productPrice;
    private String productCurrency;
    private String productStatus;
    private String url;
}
