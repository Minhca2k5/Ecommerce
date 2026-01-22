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
import com.minzetsu.ecommerce.notification.event.WebhookEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Cacheable(
            cacheNames = "bannerPublic",
            key = "'v1:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort + ':' + #filter.title + ':' + #filter.isActive + ':' + #filter.position + ':' + #filter.startAtFrom + ':' + #filter.startAtTo + ':' + #filter.endAtFrom + ':' + #filter.endAtTo",
            condition = "#isUser == true",
            sync = true
    )
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
    @CacheEvict(cacheNames = {"bannerPublic", "home"}, allEntries = true)
    public BannerResponse createBanner(BannerCreateRequest request) {
        Banner banner = bannerMapper.toEntity(request);
        banner = bannerRepository.save(banner);
        eventPublisher.publishEvent(new WebhookEvent(
                "BANNER_CREATED",
                "BANNER",
                banner.getId(),
                null
        ));
        return bannerMapper.toResponse(banner);
    }

    @Override
    @CacheEvict(cacheNames = {"bannerPublic", "home"}, allEntries = true)
    public BannerResponse updateBanner(Long id, BannerUpdateRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Banner not found"));
        bannerMapper.updateEntity(banner, request);
        banner = bannerRepository.save(banner);
        eventPublisher.publishEvent(new WebhookEvent(
                "BANNER_UPDATED",
                "BANNER",
                banner.getId(),
                null
        ));
        return bannerMapper.toResponse(banner);
    }

    @Override
    @CacheEvict(cacheNames = {"bannerPublic", "home"}, allEntries = true)
    public void deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Banner not found"));
        bannerRepository.delete(banner);
        eventPublisher.publishEvent(new WebhookEvent(
                "BANNER_DELETED",
                "BANNER",
                id,
                null
        ));
    }
}
