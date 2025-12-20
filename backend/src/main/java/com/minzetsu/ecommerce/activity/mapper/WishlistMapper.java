package com.minzetsu.ecommerce.activity.mapper;

import com.minzetsu.ecommerce.activity.dto.request.WishlistRequest;
import com.minzetsu.ecommerce.activity.dto.response.WishlistResponse;
import com.minzetsu.ecommerce.activity.entity.Wishlist;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface WishlistMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Wishlist toEntity(WishlistRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "productCurrency", source = "product.currency")
    @Mapping(target = "productStatus", source = "product.status")
    @Mapping(target = "url", ignore = true)
    WishlistResponse toResponse(Wishlist wishlist);

    List<WishlistResponse> toResponseList(List<Wishlist> list);
}
