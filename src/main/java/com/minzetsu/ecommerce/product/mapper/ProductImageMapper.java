package com.minzetsu.ecommerce.product.mapper;

import com.minzetsu.ecommerce.product.dto.request.ProductImageRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminProductImageResponse;
import com.minzetsu.ecommerce.product.dto.response.UserProductImageResponse;
import com.minzetsu.ecommerce.product.entity.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface ProductImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductImage toEntity(ProductImageRequest request);

    @Mapping(target = "productId", source = "product.id")
    AdminProductImageResponse toAdminResponse(ProductImage image);

    List<AdminProductImageResponse> toAdminResponseList(List<ProductImage> images);

    UserProductImageResponse toUserResponse(ProductImage image);

    List<UserProductImageResponse> toUserResponseList(List<ProductImage> images);
}
