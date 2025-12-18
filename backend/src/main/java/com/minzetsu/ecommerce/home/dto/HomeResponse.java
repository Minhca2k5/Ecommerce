package com.minzetsu.ecommerce.home.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.product.dto.response.CategoryResponse;
import com.minzetsu.ecommerce.product.dto.response.ProductResponse;
import com.minzetsu.ecommerce.promotion.dto.response.BannerResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomeResponse {
    private List<BannerResponse> banners;
    private List<CategoryResponse> categories;
    private List<ProductResponse> newArrivals;
    private List<ProductResponse> bestSellers;
    private List<ProductResponse> topRated;
    private List<ProductResponse> mostViewed;
}
