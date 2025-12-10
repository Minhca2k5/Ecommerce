package com.minzetsu.ecommerce.order.service;

import com.minzetsu.ecommerce.order.dto.filter.OrderItemFilter;
import com.minzetsu.ecommerce.order.dto.response.OrderItemResponse;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderItemService {
    boolean existsByOrderId(Long orderId);
    boolean existsById(Long id);
    List<OrderItem> getOrderItemsByOrderId(Long orderId);
    OrderItem getOrderItemByIdAndUserId(Long orderItemId, Long userId);
    OrderItem getOrderItemById(Long orderItemId);

    Page<OrderItemResponse> searchOrderItemResponses(OrderItemFilter filter, Pageable pageable);
    List<OrderItemResponse> getOrderItemResponsesByOrderIdAndUserId(Long orderId, Long userId);
    OrderItemResponse getOrderItemResponseByIdAndUserId(Long orderItemId, Long userId);
    Page<OrderItemResponse> getOrderItemResponsesByOrderIdAndUserId(Long orderId, Long userId, Pageable pageable);
}
