package com.minzetsu.ecommerce.order.service;

import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderItem;
import com.minzetsu.ecommerce.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderItemsService {

    private final CartItemService cartItemService;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public List<OrderItem> createOrderItems(Order order, Long cartId) {
        List<CartItem> cartItems = cartItemService.getCartItemsByCartId(cartId);
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> getOrderItem(order, cartItem))
                .toList();
        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);
        cartItemService.deleteByCartItems(cartItems);
        return savedOrderItems;
    }

    private OrderItem getOrderItem(Order order, CartItem cartItem) {
        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(order);
        orderItem.setUnitPriceSnapshot(cartItem.getUnitPriceSnapshot());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setProduct(cartItem.getProduct());
        orderItem.setProductNameSnapshot(cartItem.getProduct().getName());

        BigDecimal lineTotal = cartItem.getUnitPriceSnapshot()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        orderItem.setLineTotal(lineTotal);
        return orderItem;
    }
}
