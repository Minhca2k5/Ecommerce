package com.minzetsu.ecommerce.order.service.impl;

import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.order.dto.filter.OrderFilter;
import com.minzetsu.ecommerce.order.dto.request.OrderRequest;
import com.minzetsu.ecommerce.order.dto.response.OrderItemResponse;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderItem;
import com.minzetsu.ecommerce.order.entity.OrderStatus;
import com.minzetsu.ecommerce.order.mapper.OrderItemMapper;
import com.minzetsu.ecommerce.order.mapper.OrderMapper;
import com.minzetsu.ecommerce.order.repository.OrderItemRepository;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import com.minzetsu.ecommerce.order.repository.OrderSpecification;
import com.minzetsu.ecommerce.order.service.CreateOrderItemsService;
import com.minzetsu.ecommerce.order.service.OrderService;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import com.minzetsu.ecommerce.promotion.entity.VoucherDiscountType;
import com.minzetsu.ecommerce.promotion.service.VoucherService;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserService userService;
    private final CartService cartService;
    private final VoucherService voucherService;
    private final CartItemService cartItemService;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemRepository orderItemRepository;
    private final CreateOrderItemsService createOrderItemsService;

    private Order getExistingOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));
    }

    private void validateUserAndCart(Long userId, Long cartId) {
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        if (!cartService.existsById(cartId)) {
            throw new NotFoundException("Cart not found with id: " + cartId);
        }
    }

    private Map<String, BigDecimal> calculateTotalAmount(List<CartItem> cartItems, BigDecimal shippingFee, Long voucherId) {
        if (voucherId == null) {
            BigDecimal totalAmount = cartItems.stream()
                    .map(item -> item.getUnitPriceSnapshot()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .add(shippingFee);
            return Map.of(
                    "totalAmount", totalAmount,
                    "discountAmount", BigDecimal.ZERO
            );
        }
        Voucher voucher = voucherService.getVoucherById(voucherId);
        VoucherDiscountType discountType = voucher.getDiscountType();
        BigDecimal discountValue = voucher.getDiscountValue();
        BigDecimal Amount = cartItems.stream()
                .map(item -> item.getUnitPriceSnapshot()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal originTotal = Amount.add(shippingFee);
        if (discountType == VoucherDiscountType.FIXED) {
            if (Amount.compareTo(discountValue) < 0) {
                Amount = BigDecimal.ZERO;
            } else {
                Amount = Amount.subtract(discountValue);
            }
        } else if (discountType == VoucherDiscountType.PERCENT) {
            BigDecimal discountAmount = Amount.multiply(discountValue).divide(BigDecimal.valueOf(100));
            BigDecimal maxDiscount = voucher.getMaxDiscountAmount();
            if (maxDiscount != null && discountAmount.compareTo(maxDiscount) > 0) {
                discountAmount = maxDiscount;
            }
            Amount = Amount.subtract(discountAmount);
        } else {
            shippingFee = BigDecimal.ZERO;
        }
        BigDecimal totalAmount = Amount.add(shippingFee);
        return Map.of(
                "totalAmount", totalAmount,
                "discountAmount", originTotal.subtract(totalAmount)
        );
    }

    @Override
    public boolean existsById(Long id) {
        return orderRepository.existsById(id);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return orderRepository.existsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return getExistingOrder(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByIdAndUserId(Long id, Long userId) {
        Order order = getExistingOrder(id);
        if (!order.getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("User not authorized to access this order");
        }
        return order;
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, OrderStatus status) {
        if (!existsById(id)) {
            throw new NotFoundException("Order not found with id: " + id);
        }
        orderRepository.updateStatusById(id, status);
    }

    @Override
    @Transactional
    public void updateOrderCurrency(Long id, String currency) {
        if (!existsById(id)) {
            throw new NotFoundException("Order not found with id: " + id);
        }
        orderRepository.updateCurrencyById(id, currency);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> searchOrderResponses(OrderFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                orderRepository,
                OrderSpecification.filter(filter),
                orderMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderResponsesByUserId(Long userId) {
        if (!existsByUserId(userId)) {
            throw new NotFoundException("No orders found for userId: " + userId);
        }
        return orderMapper.toResponseList(orderRepository.findByUserIdOrderByUpdatedAtDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getFullOrderResponseByIdAndUserId(Long id, Long userId) {
        Order order = (userId != null)
                ? getOrderByIdAndUserId(id, userId)
                : getOrderById(id);
        List<OrderItemResponse> orderItems =
                orderItemMapper.toResponseList(orderItemRepository.findByOrderIdOrderByUpdatedAtDesc(id));
        return orderMapper.toFullResponse(order, orderItems);
    }

    @Override
    @Transactional
    public OrderResponse createOrderResponse(OrderRequest request, Long userId) {
        request.setUserId(userId);
        Long cartId = cartService.getCartByUserId(userId).getId();
        request.setCartId(cartId);
        validateUserAndCart(userId, cartId);

        User user = userService.getUserById(userId);
        List<CartItem> cartItems = cartItemService.getCartItemsByActiveProductTrueAndCartId(cartId, ProductStatus.ACTIVE);

        Order order = orderMapper.toEntity(request);
        order.setUser(user);
        BigDecimal shippingFee = request.getShippingFee();
        Long voucherId = request.getVoucherId();
        if (voucherId != null) {
            Voucher voucher = voucherService.getVoucherById(voucherId);
            order.setVoucher(voucher);
        }
        order.setTotalAmount(calculateTotalAmount(cartItems, shippingFee, voucherId).get("totalAmount"));
        order.setDiscountAmount(calculateTotalAmount(cartItems, shippingFee, voucherId).get("discountAmount"));
        Order savedOrder = orderRepository.save(order);
        List<OrderItem> orderItems = createOrderItemsService.createOrderItems(savedOrder, cartId);
        List<OrderItemResponse> orderItemResponses = orderItemMapper.toResponseList(orderItems);

        return orderMapper.toFullResponse(savedOrder, orderItemResponses);
    }
}
