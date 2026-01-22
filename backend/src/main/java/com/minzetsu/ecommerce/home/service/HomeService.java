package com.minzetsu.ecommerce.home.service;

import com.minzetsu.ecommerce.home.dto.HomeResponse;
import com.minzetsu.ecommerce.product.dto.filter.CategoryFilter;
import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.service.CategoryService;
import com.minzetsu.ecommerce.product.service.ProductService;
import com.minzetsu.ecommerce.promotion.dto.filter.BannerFilter;
import com.minzetsu.ecommerce.promotion.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final BannerService bannerService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final CacheManager cacheManager;

    @Cacheable(cacheNames = "home", key = "'v1'", sync = true)
    public HomeResponse getHomeData() {
        try {
            var bannerFilter = BannerFilter.builder().isActive(true).build();
            var bannerPage = bannerService.searchBanners(
                    bannerFilter,
                    PageRequest.of(0, 5, Sort.by("position").ascending()),
                    true
            );

            var categoryFilter = CategoryFilter.builder().build();
            var categoryPage = categoryService.searchCategoryResponses(categoryFilter, PageRequest.of(0, 10));

            var newArrivalsFilter = ProductFilter.builder()
                    .status("ACTIVE")
                    .sortBy("createdAt")
                    .sortDirection("DESC")
                    .build();
            var newArrivalsPage = productService.searchProductResponses(newArrivalsFilter, PageRequest.of(0, 8));

            var bestSellers = productService.getBestSellingProductResponses(30, 8);
            var topRated = productService.getTopRatingProductResponses(30, 8);
            var mostViewed = productService.getMostViewedProductResponses(30, 8);

            return HomeResponse.builder()
                    .banners(bannerPage.getContent())
                    .categories(categoryPage.getContent())
                    .newArrivals(newArrivalsPage.getContent())
                    .bestSellers(bestSellers)
                    .topRated(topRated)
                    .mostViewed(mostViewed)
                    .build();
        } catch (RuntimeException ex) {
            HomeResponse cached = getCachedHome();
            if (cached != null) {
                return cached;
            }
            throw ex;
        }
    }

    private HomeResponse getCachedHome() {
        Cache cache = cacheManager.getCache("home");
        if (cache == null) {
            return null;
        }
        return cache.get("v1", HomeResponse.class);
    }
}
