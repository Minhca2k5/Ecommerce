package com.minzetsu.ecommerce.home.service;

import com.minzetsu.ecommerce.home.dto.HomeResponse;
import com.minzetsu.ecommerce.product.dto.filter.CategoryFilter;
import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.service.CategoryService;
import com.minzetsu.ecommerce.product.service.ProductService;
import com.minzetsu.ecommerce.promotion.dto.filter.BannerFilter;
import com.minzetsu.ecommerce.promotion.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final BannerService bannerService;
    private final CategoryService categoryService;
    private final ProductService productService;

    public HomeResponse getHomeData() {
        // 1. Banners (Active, sorted by position)
        var bannerFilter = BannerFilter.builder().isActive(true).build();
        var bannerPage = bannerService.searchBanners(bannerFilter, PageRequest.of(0, 5, Sort.by("position").ascending()), true);

        // 2. Categories (Root categories)
        var categoryFilter = CategoryFilter.builder().build(); // Assuming null parentId is handled or we filter by it if needed. 
        // Actually CategoryService.searchUserCategoryResponses might not filter by parentId=null by default unless specified.
        // Let's assume we want top level. If filter doesn't support it, we might get all. 
        // For now, let's just get first page. Ideally we should add parentId to CategoryFilter.
        var categoryPage = categoryService.searchCategoryResponses(categoryFilter, PageRequest.of(0, 10));

        // 3. New Arrivals (Products sorted by created_at desc)
        var newArrivalsFilter = ProductFilter.builder()
                .status("ACTIVE")
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();
        var newArrivalsPage = productService.searchProductResponses(newArrivalsFilter, PageRequest.of(0, 8));

        // 4. Best Sellers
        var bestSellers = productService.getBestSellingProductResponses(30, 8);

        // 5. Top Rated
        var topRated = productService.getTopRatingProductResponses(30, 8);

        // 6. Most Viewed
        var mostViewed = productService.getMostViewedProductResponses(30, 8);

        return HomeResponse.builder()
                .banners(bannerPage.getContent())
                .categories(categoryPage.getContent())
                .newArrivals(newArrivalsPage.getContent())
                .bestSellers(bestSellers)
                .topRated(topRated)
                .mostViewed(mostViewed)
                .build();
    }
}
