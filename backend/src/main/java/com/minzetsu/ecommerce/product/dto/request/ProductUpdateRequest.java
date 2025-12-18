package com.minzetsu.ecommerce.product.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {

    private String name;
    private String slug;
    private String sku;
    private String description;
    private BigDecimal price;
    private String currency;
}
