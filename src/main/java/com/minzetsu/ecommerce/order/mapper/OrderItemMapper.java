package com.minzetsu.ecommerce.order.mapper;

import com.minzetsu.ecommerce.order.dto.response.OrderItemResponse;
import com.minzetsu.ecommerce.order.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface OrderItemMapper {

    // entity -> response
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "url", ignore = true)
    OrderItemResponse toResponse(OrderItem entity);

    List<OrderItemResponse> toResponseList(List<OrderItem> entities);
}
