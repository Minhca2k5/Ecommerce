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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
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

    private List<ProductResponse> toUserResponseList(List<Product> products) {
        return products.stream()
                .map(this::toUserResponse)
                .toList();
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

    private void validateActiveProduct(Product product) {
        if (!ProductStatus.ACTIVE.equals(product.getStatus())) {
            throw new UnAuthorizedException("Access denied to product with id: " + product.getId());
        }
    }

    @Override
    @Transactional
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
    public void updateProductStatus(ProductStatus status, Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        productRepository.updateByStatusAndId(status, id);
    }

    @Override
    @Transactional
    public AdminProductResponse createAdminProductResponse(ProductCreateRequest request) {
        Long categoryId = request.getCategoryId();
        if (!categoryService.existsById(categoryId)) {
            throw new NotFoundException("Category not found with id: " + categoryId);
        }

        Category category = categoryService.getCategoryById(categoryId);
        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        return toAdminResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public AdminProductResponse updateAdminProductResponse(ProductUpdateRequest request, Long id) {
        Product product = getExistingProduct(id);
        productMapper.updateEntityFromRequest(request, product);
        return toAdminResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductResponse getFullAdminProductResponseById(Long id) {
        Product product = getExistingProduct(id);
        List<InventoryResponse> inventories = inventoryMapper.toAdminResponseList(inventoryRepository.findByProductId(id));
        List<ReviewResponse> reviews = reviewMapper.toResponseList(reviewRepository.findByProductId(id));
        List<AdminProductImageResponse> images = productImageMapper.toAdminResponseList(productImageRepository.findByProductId(id));
        return toFullAdminResponse(product, reviews, inventories, images);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminProductResponse> searchAdminProductResponses(ProductFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                productRepository,
                ProductSpecification.filter(filter),
                this::toAdminResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProductResponses(ProductFilter filter, Pageable pageable) {
        filter.setStatus("ACTIVE");
        return PageableUtils.search(
                filter,
                pageable,
                productRepository,
                ProductSpecification.filter(filter),
                this::toUserResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getFullProductResponseById(Long id) {
        Product product = getExistingProduct(id);
        validateActiveProduct(product);
        List<InventoryResponse> inventories = inventoryMapper.toAdminResponseList(inventoryRepository.findByProductId(id));
        List<ReviewResponse> reviews = reviewMapper.toResponseList(reviewRepository.findByProductId(id));
        List<ProductImageResponse> images = productImageMapper.toResponseList(productImageRepository.findByProductId(id));
        return toFullUserResponse(product, reviews, inventories, images);
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

        List<Product> productList = productRepository.findAllById(productIds);

        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Product> orderedProducts = productIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<ProductResponse> responses = toUserResponseList(orderedProducts);

        IntStream.range(0, responses.size())
                .forEach(i ->
                        metricSetter.accept(responses.get(i), views.get(i))
                );

        return responses;
    }

}
