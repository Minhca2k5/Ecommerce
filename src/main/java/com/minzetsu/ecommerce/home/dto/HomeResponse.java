package com.minzetsu.ecommerce.home.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.product.dto.response.UserCategoryResponse;
import com.minzetsu.ecommerce.product.dto.response.UserProductResponse;
import com.minzetsu.ecommerce.promotion.dto.response.BannerResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomeResponse {
    private List<BannerResponse> banners;
    private List<UserCategoryResponse> categories;
    private List<UserProductResponse> newArrivals;
    private List<UserProductResponse> bestSellers;
    private List<UserProductResponse> topRated;
    private List<UserProductResponse> mostViewed;
}
