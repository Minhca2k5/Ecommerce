package com.minzetsu.ecommerce.product.service.impl;

import com.minzetsu.ecommerce.activity.repository.RecentViewRepository;
import com.minzetsu.ecommerce.activity.repository.WishlistRepository;
import com.minzetsu.ecommerce.cart.repository.CartItemRepository;
import com.minzetsu.ecommerce.common.exception.DeletionException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
import com.minzetsu.ecommerce.inventory.mapper.InventoryMapper;
import com.minzetsu.ecommerce.inventory.repository.InventoryRepository;
import com.minzetsu.ecommerce.order.repository.OrderItemRepository;
import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.dto.request.ProductCreateRequest;
import com.minzetsu.ecommerce.product.dto.request.ProductUpdateRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminProductImageResponse;
import com.minzetsu.ecommerce.product.dto.response.AdminProductResponse;
import com.minzetsu.ecommerce.product.dto.response.ProductImageResponse;
import com.minzetsu.ecommerce.product.dto.response.ProductResponse;
import com.minzetsu.ecommerce.product.entity.Category;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.entity.ProductImage;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.product.mapper.ProductImageMapper;
import com.minzetsu.ecommerce.product.mapper.ProductMapper;
import com.minzetsu.ecommerce.product.repository.ProductImageRepository;
import com.minzetsu.ecommerce.product.repository.ProductRepository;
import com.minzetsu.ecommerce.product.repository.ProductSpecification;
import com.minzetsu.ecommerce.product.repository.projection.ProductBestSellingView;
import com.minzetsu.ecommerce.product.repository.projection.ProductMostFavoriteView;
import com.minzetsu.ecommerce.product.repository.projection.ProductMostViewedView;
import com.minzetsu.ecommerce.product.repository.projection.ProductRatingView;
import com.minzetsu.ecommerce.product.service.CategoryService;
import com.minzetsu.ecommerce.product.service.ProductService;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import com.minzetsu.ecommerce.review.mapper.ReviewMapper;
import com.minzetsu.ecommerce.review.repository.ReviewRepository;
import com.minzetsu.ecommerce.notification.event.WebhookEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final ReviewRepository reviewRepository;
    private final ProductImageRepository productImageRepository;
    private final RecentViewRepository recentViewRepository;
    private final WishlistRepository wishlistRepository;
    private final CategoryService categoryService;
    private final ReviewMapper reviewMapper;
    private final ProductImageMapper productImageMapper;
    private final InventoryMapper inventoryMapper;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${days}")
    private Integer days;

    private void assignUrlToResponse(Long productId, Object response) {
        Optional<ProductImage> mainImage = productImageRepository.findByIsPrimaryTrueAndProductId(productId);
        mainImage.ifPresent(image -> {
            if (response instanceof AdminProductResponse adminResponse) {
                adminResponse.setUrl(image.getUrl());
            } else if (response instanceof ProductResponse userResponse) {
                userResponse.setUrl(image.getUrl());
            }
        });
    }

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

    private void applyUrlsToAdminResponses(List<AdminProductResponse> responses) {
        List<Long> productIds = responses.stream()
                .map(AdminProductResponse::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, String> urlMap = getPrimaryImageUrlMap(productIds);
        responses.forEach(response -> response.setUrl(urlMap.get(response.getId())));
    }

    private void applyUrlsToUserResponses(List<ProductResponse> responses) {
        List<Long> productIds = responses.stream()
                .map(ProductResponse::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, String> urlMap = getPrimaryImageUrlMap(productIds);
        responses.forEach(response -> response.setUrl(urlMap.get(response.getId())));
    }
    private void handleFields(Long productId, Object response) {
        ProductRatingView ratingView = reviewRepository.getProductRatingViewByProductId(productId, days);
        Double recentlyAverageRating = ratingView.getAverageRating();
        Integer recentlyReviewCount = ratingView.getTotalRatings();
        Integer recentlyTotalSoldQuantity = orderItemRepository.getTotalQuantitySoldByProductIdLastDays(productId, days);
        Integer recentlyTotalViewedQuantity = recentViewRepository.countByProductIdLastDays(productId, days);
        Integer recentlyFavoriteCount = wishlistRepository.countByProductIdLastDays(productId, days);
        if (response instanceof AdminProductResponse adminResponse) {
            adminResponse.setRecentlyAverageRating(recentlyAverageRating);
            adminResponse.setRecentlyReviewCount(recentlyReviewCount);
            adminResponse.setRecentlyTotalSoldQuantity(recentlyTotalSoldQuantity);
            adminResponse.setRecentlyTotalViewedQuantity(recentlyTotalViewedQuantity);
            adminResponse.setRecentlyFavoriteCount(recentlyFavoriteCount);
        } else if (response instanceof ProductResponse userResponse) {
            userResponse.setRecentlyAverageRating(recentlyAverageRating);
            userResponse.setRecentlyReviewCount(recentlyReviewCount);
            userResponse.setRecentlyTotalSoldQuantity(recentlyTotalSoldQuantity);
            userResponse.setRecentlyTotalViewedQuantity(recentlyTotalViewedQuantity);
            userResponse.setRecentlyFavoriteCount(recentlyFavoriteCount);
        }
    }

    private AdminProductResponse toAdminResponse(Product product) {
        AdminProductResponse response = productMapper.toAdminResponse(product);
        Long productId = response.getId();
        assignUrlToResponse(productId, response);
        return response;
    }

    private List<AdminProductResponse> toAdminResponseListWithUrls(List<Product> products) {
        List<AdminProductResponse> responses = productMapper.toAdminResponseList(products);
        applyUrlsToAdminResponses(responses);
        return responses;
    }

    private AdminProductResponse toFullAdminResponse(
            Product product,
            List<ReviewResponse> reviews,
            List<InventoryResponse> inventories,
            List<AdminProductImageResponse> images
    ) {
        AdminProductResponse response = productMapper.toFullAdminResponse(product, reviews, inventories, images);
        Long productId = response.getId();
        assignUrlToResponse(productId, response);
        handleFields(productId, response);
        return response;
    }

    private ProductResponse toUserResponse(Product product) {
        ProductResponse response = productMapper.toResponse(product);
        Long productId = response.getId();
        assignUrlToResponse(productId, response);
        return response;
    }

    private List<ProductResponse> toUserResponseListWithUrls(List<Product> products) {
        List<ProductResponse> responses = productMapper.toResponseList(products);
        applyUrlsToUserResponses(responses);
        return responses;
    }

    private ProductResponse toFullUserResponse(
            Product product,
            List<ReviewResponse> reviews,
            List<InventoryResponse> inventories,
            List<ProductImageResponse> images
    ) {
        ProductResponse response = productMapper.toFullResponse(product, reviews, images);
        List<InventoryResponse> activeInventories = inventories.stream()
                .filter(InventoryResponse::getIsActive)
                .toList();
        int totalStockQty = activeInventories.stream().mapToInt(InventoryResponse::getStockQty).sum();
        if (totalStockQty > 0) {
            response.setInStockStatus("In Stock: " + totalStockQty);
        } else {
            response.setInStockStatus("Out of Stock");
        }
        Long productId = response.getId();
        assignUrlToResponse(productId, response);
        handleFields(productId, response);
        return response;
    }

    private Product getExistingProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    private Product getExistingProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Product not found with slug: " + slug));
    }

    private void validateActiveProduct(Product product) {
        if (!ProductStatus.ACTIVE.equals(product.getStatus())) {
            throw new UnAuthorizedException("Access denied to product with id: " + product.getId());
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"home", "productDetail"}, allEntries = true)
    public void deleteProduct(Long id) {
        Product product = getExistingProduct(id);
        if (cartItemRepository.existsByProductId(id) || orderItemRepository.existsByProductId(id)) {
            throw new DeletionException("Cannot delete product with id " + id +
                    " because it is associated with existing cart or order items.");
        }
        inventoryRepository.deleteByProductId(id);
        reviewRepository.deleteByProductId(id);
        productImageRepository.deleteByProductId(id);
        productRepository.delete(product);
        eventPublisher.publishEvent(new WebhookEvent(
                "PRODUCT_DELETED",
                "PRODUCT",
                id,
                null
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return getExistingProduct(id);
    }

    @Override
    @Transactional
    public void save(Product product) {
        productRepository.save(product);
    }

    @Override
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"home", "productDetail"}, allEntries = true)
    public void updateProductStatus(ProductStatus status, Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        productRepository.updateByStatusAndId(status, id);
        eventPublisher.publishEvent(new WebhookEvent(
                "PRODUCT_STATUS_UPDATED",
                "PRODUCT",
                id,
                null
        ));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"home", "productDetail"}, allEntries = true)
    public AdminProductResponse createAdminProductResponse(ProductCreateRequest request) {
        Long categoryId = request.getCategoryId();
        if (!categoryService.existsById(categoryId)) {
            throw new NotFoundException("Category not found with id: " + categoryId);
        }

        Category category = categoryService.getCategoryById(categoryId);
        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        Product saved = productRepository.save(product);
        eventPublisher.publishEvent(new WebhookEvent(
                "PRODUCT_CREATED",
                "PRODUCT",
                saved.getId(),
                null
        ));
        return toAdminResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"home", "productDetail"}, allEntries = true)
    public AdminProductResponse updateAdminProductResponse(ProductUpdateRequest request, Long id) {
        Product product = getExistingProduct(id);
        productMapper.updateEntityFromRequest(request, product);
        Product saved = productRepository.save(product);
        eventPublisher.publishEvent(new WebhookEvent(
                "PRODUCT_UPDATED",
                "PRODUCT",
                saved.getId(),
                null
        ));
        return toAdminResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductResponse getFullAdminProductResponseById(Long id) {
        Product product = getExistingProduct(id);
        List<InventoryResponse> inventories = inventoryMapper.toAdminResponseList(inventoryRepository.findByProductId(id));
        List<ReviewResponse> reviews = reviewMapper.toResponseList(reviewRepository.findByProductIdOrderByUpdatedAtDesc(id));
        List<AdminProductImageResponse> images = productImageMapper.toAdminResponseList(productImageRepository.findByProductId(id));
        return toFullAdminResponse(product, reviews, inventories, images);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminProductResponse> searchAdminProductResponses(ProductFilter filter, Pageable pageable) {
        Pageable sortedPageable = PageableUtils.applySorting(pageable, filter);
        Page<Product> page = productRepository.findAll(ProductSpecification.filter(filter), sortedPageable);
        List<AdminProductResponse> responses = toAdminResponseListWithUrls(page.getContent());
        return new PageImpl<>(responses, sortedPageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProductResponses(ProductFilter filter, Pageable pageable) {
        filter.setStatus("ACTIVE");
        Pageable sortedPageable = PageableUtils.applySorting(pageable, filter);
        Page<Product> page = productRepository.findAll(ProductSpecification.filter(filter), sortedPageable);
        List<ProductResponse> responses = toUserResponseListWithUrls(page.getContent());
        return new PageImpl<>(responses, sortedPageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productDetail", key = "'v1:' + #id", sync = true)
    public ProductResponse getFullProductResponseById(Long id) {
        try {
            Product product = getExistingProduct(id);
            validateActiveProduct(product);
            List<InventoryResponse> inventories = inventoryMapper.toAdminResponseList(inventoryRepository.findByProductId(id));
            List<ReviewResponse> reviews = reviewMapper.toResponseList(reviewRepository.findByProductIdOrderByUpdatedAtDesc(id));
            List<ProductImageResponse> images = productImageMapper.toResponseList(productImageRepository.findByProductId(id));
            return toFullUserResponse(product, reviews, inventories, images);
        } catch (RuntimeException ex) {
            ProductResponse cached = getCachedProductById(id);
            if (cached != null) {
                return cached;
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productDetail", key = "'v1:slug:' + #slug", sync = true)
    public ProductResponse getFullProductResponseBySlug(String slug) {
        try {
            Product product = getExistingProductBySlug(slug);
            validateActiveProduct(product);
            Long productId = product.getId();
            List<InventoryResponse> inventories = inventoryMapper.toAdminResponseList(inventoryRepository.findByProductId(productId));
            List<ReviewResponse> reviews = reviewMapper.toResponseList(reviewRepository.findByProductIdOrderByUpdatedAtDesc(productId));
            List<ProductImageResponse> images = productImageMapper.toResponseList(productImageRepository.findByProductId(productId));
            return toFullUserResponse(product, reviews, inventories, images);
        } catch (RuntimeException ex) {
            ProductResponse cached = getCachedProductBySlug(slug);
            if (cached != null) {
                return cached;
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getTopRatingProductResponses(Integer days, Integer limit) {
        List<ProductRatingView> views =
                reviewRepository.getProductRatingViewsByAverageRatingLastDaysAndLimit(days, limit);

        return buildRankedUserResponses(
                views,
                ProductRatingView::getProductId,
                (res, v) -> res.setRecentlyAverageRating(v.getAverageRating())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getMostFavoriteProductResponses(Integer days, Integer limit) {
        List<ProductMostFavoriteView> views =
                wishlistRepository.getProductMostFavoriteViewsByTotalFavoriteLastDaysAndLimit(days, limit);

        return buildRankedUserResponses(
                views,
                ProductMostFavoriteView::getProductId,
                (res, v) -> res.setRecentlyFavoriteCount(v.getTotalFavorites())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getMostViewedProductResponses(Integer days, Integer limit) {
        List<ProductMostViewedView> views =
                recentViewRepository.getProductMostViewedViewsByTotalViewedLastDaysAndLimit(days, limit);

        return buildRankedUserResponses(
                views,
                ProductMostViewedView::getProductId,
                (res, v) -> res.setRecentlyTotalViewedQuantity(v.getTotalViews())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getBestSellingProductResponses(Integer days, Integer limit) {
        List<ProductBestSellingView> views =
                orderItemRepository.getProductBestSellingViewsByTotalQuantitySoldLastDaysAndLimit(days, limit);

        return buildRankedUserResponses(
                views,
                ProductBestSellingView::getProductId,
                (res, v) -> res.setRecentlyTotalSoldQuantity(v.getTotalSold())
        );
    }

    private <T> List<ProductResponse> buildRankedUserResponses(
            List<T> views,
            Function<T, Long> idExtractor,
            BiConsumer<ProductResponse, T> metricSetter
    ) {
        List<Long> productIds = views.stream()
                .map(idExtractor)
                .toList();

        List<Product> productList = productRepository.findAllByIdInWithCategory(productIds);

        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Product> orderedProducts = productIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<ProductResponse> responses = toUserResponseListWithUrls(orderedProducts);

        IntStream.range(0, responses.size())
                .forEach(i ->
                        metricSetter.accept(responses.get(i), views.get(i))
                );

        return responses;
    }

    private ProductResponse getCachedProductById(Long id) {
        Cache cache = cacheManager.getCache("productDetail");
        if (cache == null) {
            return null;
        }
        return cache.get("v1:" + id, ProductResponse.class);
    }

    private ProductResponse getCachedProductBySlug(String slug) {
        Cache cache = cacheManager.getCache("productDetail");
        if (cache == null) {
            return null;
        }
        return cache.get("v1:slug:" + slug, ProductResponse.class);
    }

}
