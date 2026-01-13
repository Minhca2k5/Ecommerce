package com.minzetsu.ecommerce.activity.service.impl;

import com.minzetsu.ecommerce.activity.dto.request.RecentViewRequest;
import com.minzetsu.ecommerce.activity.dto.response.RecentViewResponse;
import com.minzetsu.ecommerce.activity.entity.RecentView;
import com.minzetsu.ecommerce.activity.mapper.RecentViewMapper;
import com.minzetsu.ecommerce.activity.repository.RecentViewRepository;
import com.minzetsu.ecommerce.activity.service.RecentViewService;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.product.entity.ProductImage;
import com.minzetsu.ecommerce.product.repository.ProductImageRepository;
import com.minzetsu.ecommerce.product.service.ProductService;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecentViewServiceImpl implements RecentViewService {

    private final RecentViewRepository recentViewRepository;
    private final RecentViewMapper recentViewMapper;
    private final ProductService productService;
    private final UserService userService;
    private final ProductImageRepository productImageRepository;

    private Map<Long, String> getPrimaryImageUrlMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productImageRepository.findPrimaryByProductIds(productIds).stream()
                .filter(image -> image.getProduct() != null && image.getProduct().getId() != null)
                .collect(Collectors.toMap(
                        image -> image.getProduct().getId(),
                        ProductImage::getUrl,
                        (existing, ignored) -> existing
                ));
    }

    private RecentViewResponse toResponseWithUrl(RecentView recentView) {
        RecentViewResponse response = recentViewMapper.toResponse(recentView);
        Long productId = response.getProductId();
        Optional<ProductImage> mainImage = productImageRepository.findByIsPrimaryTrueAndProductId(productId);
        mainImage.ifPresent(image -> response.setUrl(image.getUrl()));
        return response;
    }

    private List<RecentViewResponse> toResponseListWithUrl(List<RecentView> recentViews) {
        List<RecentViewResponse> responses = recentViewMapper.toResponseList(recentViews);
        List<Long> productIds = responses.stream()
                .map(RecentViewResponse::getProductId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, String> urlMap = getPrimaryImageUrlMap(productIds);
        responses.forEach(response -> response.setUrl(urlMap.get(response.getProductId())));
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecentViewResponse> getRecentViewsByUserId(Long userId, Pageable pageable) {
        Page<RecentView> recentViewResponses = recentViewRepository.findByUserId(userId, pageable);
        List<RecentViewResponse> responses = toResponseListWithUrl(recentViewResponses.getContent());
        return new PageImpl<>(responses, recentViewResponses.getPageable(), recentViewResponses.getTotalElements());
    }

    @Override
    @Transactional
    public void deleteRecentView(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("Recent view ID cannot be null.");
        }
        RecentView recentView = recentViewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recent view with ID " + id + " not found."));
        if (!recentView.getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("User with ID " + userId + " is not authorized to delete this recent view.");
        }
        recentViewRepository.deleteById(id);
    }

    @Override
    @Transactional
    public RecentViewResponse addRecentView(RecentViewRequest request, Long userId) {
        Long productId = request.getProductId();
        if (!productService.existsById(productId)) {
            throw new NotFoundException("Product with ID " + productId + " not found.");
        }
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User with ID " + userId + " not found.");
        }
        Optional<RecentView> existing = recentViewRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            RecentView view = existing.get();
            view.setProduct(view.getProduct()); // mark dirty
            RecentView saved = recentViewRepository.save(view);
            return toResponseWithUrl(saved);
        }
        RecentView recentView = recentViewMapper.toEntity(request);
        recentView.setProduct(productService.getProductById(productId));
        recentView.setUser(userService.getUserById(userId));
        return toResponseWithUrl(recentViewRepository.save(recentView));
    }


    @Override
    @Transactional
    public void deleteAllRecentViewsByUserId(Long userId) {
        recentViewRepository.deleteByUserId(userId);
    }

    @Override
    public List<RecentViewResponse> getRecentViewsByProductName(String productName, Long userId) {
        return toResponseListWithUrl(recentViewRepository.findByProductNameOrderByUpdatedAtDesc(productName, userId));
    }
}
