package com.minzetsu.ecommerce.promotion.service;

import com.minzetsu.ecommerce.promotion.dto.filter.BannerFilter;
import com.minzetsu.ecommerce.promotion.dto.request.BannerCreateRequest;
import com.minzetsu.ecommerce.promotion.dto.request.BannerUpdateRequest;
import com.minzetsu.ecommerce.promotion.dto.response.BannerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BannerService {
    Page<BannerResponse> searchBanners(BannerFilter filter, Pageable pageable, boolean isUser);
    BannerResponse createBanner(BannerCreateRequest request);
    BannerResponse updateBanner(Long id, BannerUpdateRequest request);
    void deleteBanner(Long id);
}
