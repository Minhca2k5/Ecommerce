package com.minzetsu.ecommerce.cart.mapper;

import com.minzetsu.ecommerce.cart.dto.request.CartItemRequest;
import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.entity.CartItem;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface CartItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "unitPriceSnapshot", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "product", ignore = true)
    CartItem toEntity(CartItemRequest request);

    @Mapping(target = "cartId", source = "cart.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "productCurrency", source = "product.currency")
    @Mapping(target = "productStatus", source = "product.status")
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "lineTotal", expression = "java(calcLineTotal(entity))")
    CartItemResponse toResponse(CartItem entity);

    List<CartItemResponse> toResponseList(List<CartItem> entities);

    default BigDecimal calcLineTotal(CartItem entity) {
        if (entity == null ||
                entity.getUnitPriceSnapshot() == null ||
                entity.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return entity.getUnitPriceSnapshot()
                .multiply(BigDecimal.valueOf(entity.getQuantity()));
    }
}
