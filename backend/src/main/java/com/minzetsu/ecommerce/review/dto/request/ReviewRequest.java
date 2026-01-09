package com.minzetsu.ecommerce.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;
    private Long userId;
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be >= 1")
    @Max(value = 5, message = "Rating must be <= 5")
    private Integer rating = 5;
    @NotBlank(message = "Comment is required")
    private String comment;
}
