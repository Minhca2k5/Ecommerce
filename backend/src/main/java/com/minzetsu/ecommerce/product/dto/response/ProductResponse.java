package com.minzetsu.ecommerce.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse extends BaseDTO {

    private String name;
    private String slug;
    private String sku;
    private String description;

    private BigDecimal price;
    private String currency;
    private String status;

    private Long categoryId;
    private String categoryName;
    private String categorySlug;

    private Double recentlyAverageRating;
    private String inStockStatus;
    private Integer recentlyReviewCount;
    private Integer recentlyTotalSoldQuantity;
    private Integer recentlyTotalViewedQuantity;
    private Integer recentlyFavoriteCount;
    private String url;

    private List<ReviewResponse> reviews;
    private List<ProductImageResponse> images;

}
