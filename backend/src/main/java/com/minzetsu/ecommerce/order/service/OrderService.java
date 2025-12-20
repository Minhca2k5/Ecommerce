package com.minzetsu.ecommerce.order.service;

import com.minzetsu.ecommerce.order.dto.filter.OrderFilter;
import com.minzetsu.ecommerce.order.dto.request.OrderRequest;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    boolean existsById(Long id);
    boolean existsByUserId(Long userId);
    Order getOrderById(Long id);
    Order getOrderByIdAndUserId(Long id, Long userId);
    void updateOrderStatus(Long id, OrderStatus status);
    void updateOrderCurrency(Long id, String currency);
    Page<OrderResponse> searchOrderResponses(OrderFilter filter, Pageable pageable);
    List<OrderResponse> getOrderResponsesByUserId(Long userId);
    OrderResponse getFullOrderResponseByIdAndUserId(Long id, Long userId);
    OrderResponse createOrderResponse(OrderRequest request, Long userId);
}
