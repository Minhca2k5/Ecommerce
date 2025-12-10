package com.minzetsu.ecommerce.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Image URL is required")
    private String url;

    @Builder.Default
    private Boolean isPrimary = false;
}
