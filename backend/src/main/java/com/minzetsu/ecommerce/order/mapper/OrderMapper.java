package com.minzetsu.ecommerce.order.mapper;

import com.minzetsu.ecommerce.order.dto.request.OrderRequest;
import com.minzetsu.ecommerce.order.dto.response.OrderItemResponse;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.entity.Order;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface OrderMapper {

    // request -> entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "voucher", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    Order toEntity(OrderRequest request);

    // entity -> response (base)
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "discountAmount", source = "discountAmount")
    @Mapping(target = "voucherId", source = "voucher.id")
    @Mapping(target = "itemCount", ignore = true)
    OrderResponse toResponse(Order order);

    // build full response (order + items)
    default OrderResponse toFullResponse(Order order, List<OrderItemResponse> items) {
        OrderResponse res = toResponse(order);
        res.setItems(items);
        res.setItemCount(items != null ? items.size() : 0);
        return res;
    }

    List<OrderResponse> toResponseList(List<Order> orders);
}
