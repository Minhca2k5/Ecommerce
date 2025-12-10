package com.minzetsu.ecommerce.product.mapper;

import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
import com.minzetsu.ecommerce.product.dto.request.ProductCreateRequest;
import com.minzetsu.ecommerce.product.dto.request.ProductUpdateRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminProductImageResponse;
import com.minzetsu.ecommerce.product.dto.response.AdminProductResponse;
import com.minzetsu.ecommerce.product.dto.response.UserProductImageResponse;
import com.minzetsu.ecommerce.product.dto.response.UserProductResponse;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import com.minzetsu.ecommerce.product.entity.Product;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductCreateRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categorySlug", source = "category.slug")
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "totalStockQty", ignore = true)
    @Mapping(target = "totalReservedQty", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "recentlyAverageRating", ignore = true)
    @Mapping(target = "recentlyReviewCount", ignore = true)
    @Mapping(target = "recentlyTotalSoldQuantity", ignore = true)
    @Mapping(target = "recentlyTotalViewedQuantity", ignore = true)
    @Mapping(target = "recentlyFavoriteCount", ignore = true)
    AdminProductResponse toAdminResponse(Product product);

    List<AdminProductResponse> toAdminResponseList(List<Product> products);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromRequest(ProductUpdateRequest request, @MappingTarget Product product);

    default AdminProductResponse toFullAdminResponse(
            Product product,
            List<ReviewResponse> reviews,
            List<InventoryResponse> inventories,
            List<AdminProductImageResponse> images
    ) {
        AdminProductResponse res = toAdminResponse(product);
        res.setReviews(reviews);
        res.setInventories(inventories);
        res.setImages(images);
        List<InventoryResponse> activeInventories = inventories.stream()
                .filter(InventoryResponse::getIsActive)
                .toList();
        Integer totalStockQty = activeInventories.stream().mapToInt(InventoryResponse::getStockQty).sum();
        Integer totalReservedQty = activeInventories.stream().mapToInt(InventoryResponse::getReservedQty).sum();
        res.setTotalStockQty(totalStockQty);
        res.setTotalReservedQty(totalReservedQty);
        return res;
    }

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categorySlug", source = "category.slug")
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "inStockStatus", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "recentlyAverageRating", ignore = true)
    @Mapping(target = "recentlyReviewCount", ignore = true)
    @Mapping(target = "recentlyTotalSoldQuantity", ignore = true)
    @Mapping(target = "recentlyTotalViewedQuantity", ignore = true)
    @Mapping(target = "recentlyFavoriteCount", ignore = true)
    UserProductResponse toUserResponse(Product product);

    List<UserProductResponse> toUserResponseList(List<Product> products);

    default UserProductResponse toFullUserResponse(
            Product product,
            List<ReviewResponse> reviews,
            List<UserProductImageResponse> images
    ) {
        UserProductResponse res = toUserResponse(product);
        res.setReviews(reviews);
        res.setImages(images);
        return res;
    }
}
