package com.minzetsu.ecommerce.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "SKU is required")
    private String sku;

    private String description;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @Builder.Default
    private String currency = "VND";

    @Builder.Default
    private String status = "ACTIVE";

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
