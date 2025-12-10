package com.minzetsu.ecommerce.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
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
public class AdminProductResponse extends BaseDTO {

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

    private Integer totalStockQty;
    private Integer totalReservedQty;

    private Double recentlyAverageRating;
    private Integer recentlyReviewCount;
    private Integer recentlyTotalSoldQuantity;
    private Integer recentlyTotalViewedQuantity;
    private Integer recentlyFavoriteCount;
    private String url;

    private List<ReviewResponse> reviews;
    private List<InventoryResponse> inventories;
    private List<AdminProductImageResponse> images;

}
