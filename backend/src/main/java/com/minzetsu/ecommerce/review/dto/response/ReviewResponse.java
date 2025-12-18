package com.minzetsu.ecommerce.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse extends BaseDTO {

    private Integer rating;
    private String comment;

    private Long productId;
    private String productName;
    private String productSlug;

    private Long userId;
    private String username;
    private String fullName;
}
