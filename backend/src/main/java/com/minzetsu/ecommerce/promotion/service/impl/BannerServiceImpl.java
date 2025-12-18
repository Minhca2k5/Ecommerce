package com.minzetsu.ecommerce.promotion.service.impl;

import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.promotion.dto.filter.BannerFilter;
import com.minzetsu.ecommerce.promotion.dto.request.BannerCreateRequest;
import com.minzetsu.ecommerce.promotion.dto.request.BannerUpdateRequest;
import com.minzetsu.ecommerce.promotion.dto.response.BannerResponse;
import com.minzetsu.ecommerce.promotion.entity.Banner;
import com.minzetsu.ecommerce.promotion.mapper.BannerMapper;
import com.minzetsu.ecommerce.promotion.repository.BannerRepository;
import com.minzetsu.ecommerce.promotion.repository.BannerSpecification;
import com.minzetsu.ecommerce.promotion.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;

    @Override
    public Page<BannerResponse> searchBanners(BannerFilter filter, Pageable pageable, boolean isUser) {
        if (isUser) {
            filter.setIsActive(true);
        }
        return PageableUtils.search(
                filter,
                pageable,
                bannerRepository,
                BannerSpecification.filter(filter),
                bannerMapper::toResponse
        );
    }

    @Override
    public BannerResponse createBanner(BannerCreateRequest request) {
        Banner banner = bannerMapper.toEntity(request);
        banner = bannerRepository.save(banner);
        return bannerMapper.toResponse(banner);
    }

    @Override
    public BannerResponse updateBanner(Long id, BannerUpdateRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Banner not found"));
        bannerMapper.updateEntity(banner, request);
        banner = bannerRepository.save(banner);
        return bannerMapper.toResponse(banner);
    }

    @Override
    public void deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Banner not found"));
        bannerRepository.delete(banner);
    }
}
