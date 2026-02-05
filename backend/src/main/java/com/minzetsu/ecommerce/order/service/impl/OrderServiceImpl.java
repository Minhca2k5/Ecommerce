package com.minzetsu.ecommerce.order.service.impl;

import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.common.audit.AuditAction;
import com.minzetsu.ecommerce.common.exception.AppException;
import com.minzetsu.ecommerce.common.utils.DatabaseRetryExecutor;
import com.minzetsu.ecommerce.order.config.CheckoutPricingProperties;
import com.minzetsu.ecommerce.order.config.GuestCheckoutProperties;
import com.minzetsu.ecommerce.messaging.DomainEventPublisher;
import com.minzetsu.ecommerce.messaging.DomainEventType;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.common.idempotency.IdempotencyService;
import com.minzetsu.ecommerce.order.dto.filter.OrderFilter;
import com.minzetsu.ecommerce.order.dto.request.OrderRequest;
import com.minzetsu.ecommerce.order.dto.response.OrderItemResponse;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderItem;
import com.minzetsu.ecommerce.order.entity.OrderStatus;
import com.minzetsu.ecommerce.order.event.OrderCreatedEvent;
import com.minzetsu.ecommerce.notification.event.WebhookEvent;
import com.minzetsu.ecommerce.order.mapper.OrderItemMapper;
import com.minzetsu.ecommerce.order.mapper.OrderMapper;
import com.minzetsu.ecommerce.order.repository.OrderItemRepository;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import com.minzetsu.ecommerce.order.repository.OrderSpecification;
import com.minzetsu.ecommerce.order.service.CreateOrderItemsService;
import com.minzetsu.ecommerce.order.service.GuestCheckoutIdentityService;
import com.minzetsu.ecommerce.order.service.GuestOrderAccessTokenService;
import com.minzetsu.ecommerce.order.service.OrderService;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import com.minzetsu.ecommerce.promotion.entity.VoucherDiscountType;
import com.minzetsu.ecommerce.promotion.service.VoucherService;
import com.minzetsu.ecommerce.realtime.SseEmitterService;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
    private final ApplicationEventPublisher eventPublisher;
    private final DomainEventPublisher domainEventPublisher;
    private final IdempotencyService idempotencyService;
    private final SseEmitterService sseEmitterService;
    private final DatabaseRetryExecutor databaseRetryExecutor;
    private final PlatformTransactionManager transactionManager;
    private final GuestCheckoutIdentityService guestCheckoutIdentityService;
    private final GuestOrderAccessTokenService guestOrderAccessTokenService;
    private final GuestCheckoutProperties guestCheckoutProperties;
    private final CheckoutPricingProperties checkoutPricingProperties;

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

    private Map<String, BigDecimal> calculatePricing(List<CartItem> cartItems, OrderRequest request) {
        String currency = checkoutPricingProperties.normalizeCurrency(request.getCurrency());
        BigDecimal rate = checkoutPricingProperties.getExchangeRates().get(currency);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Unsupported currency: " + currency, HttpStatus.BAD_REQUEST);
        }

        BigDecimal subtotalVnd = cartItems.stream()
                .map(item -> item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shipping = request.getShippingFee();
        if (shipping == null) {
            shipping = checkoutPricingProperties.getShippingFlatFees()
                    .getOrDefault(currency, BigDecimal.ZERO);
        }
        shipping = shipping.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shippingVnd = shipping.divide(rate, 2, RoundingMode.HALF_UP);

        BigDecimal discountVnd = calculateDiscountAmount(subtotalVnd, shippingVnd, request.getVoucherId());
        BigDecimal subtotal = convertCurrency(subtotalVnd, rate);
        BigDecimal discount = convertCurrency(discountVnd, rate);

        BigDecimal taxRate = checkoutPricingProperties.getTaxRates().getOrDefault(currency, BigDecimal.ZERO);
        BigDecimal taxableAmount = subtotal.subtract(discount).max(BigDecimal.ZERO).add(shipping);
        BigDecimal tax = taxableAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = taxableAmount.add(tax).setScale(2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> pricing = new LinkedHashMap<>();
        pricing.put("subtotalAmount", subtotal);
        pricing.put("discountAmount", discount);
        pricing.put("shippingFee", shipping);
        pricing.put("taxAmount", tax);
        pricing.put("totalAmount", total);
        return pricing;
    }

    private BigDecimal calculateDiscountAmount(BigDecimal subtotalVnd, BigDecimal shippingVnd, Long voucherId) {
        if (voucherId == null) {
            return BigDecimal.ZERO;
        }
        Voucher voucher = voucherService.getVoucherById(voucherId);
        VoucherDiscountType discountType = voucher.getDiscountType();
        BigDecimal discountValue = voucher.getDiscountValue() == null ? BigDecimal.ZERO : voucher.getDiscountValue();
        if (discountType == VoucherDiscountType.FIXED) {
            return subtotalVnd.min(discountValue);
        }
        if (discountType == VoucherDiscountType.PERCENT) {
            BigDecimal discount = subtotalVnd.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal maxDiscount = voucher.getMaxDiscountAmount();
            BigDecimal capped = (maxDiscount != null && discount.compareTo(maxDiscount) > 0) ? maxDiscount : discount;
            return capped.min(subtotalVnd);
        }
        if (discountType == VoucherDiscountType.FREESHIP) {
            return shippingVnd.max(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal convertCurrency(BigDecimal amountVnd, BigDecimal rate) {
        return amountVnd.multiply(rate).setScale(2, RoundingMode.HALF_UP);
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
    @AuditAction(action = "ORDER_STATUS_UPDATED", entityType = "ORDER", idParamIndex = 0)
    public void updateOrderStatus(Long id, OrderStatus status) {
        if (!existsById(id)) {
            throw new NotFoundException("Order not found with id: " + id);
        }
        orderRepository.updateStatusById(id, status);
        eventPublisher.publishEvent(new WebhookEvent(
                "ORDER_STATUS_UPDATED",
                "ORDER",
                id,
                null
        ));
        domainEventPublisher.publish(DomainEventType.ORDER_STATUS_UPDATED, id, null, Map.of("status", status.name()));
        orderRepository.findById(id).ifPresent(order ->
                sseEmitterService.sendToUser(order.getUser().getId(), "order-status", Map.of(
                        "orderId", id,
                        "status", status.name()
                ))
        );
    }

    @Override
    @Transactional
    @AuditAction(action = "ORDER_CURRENCY_UPDATED", entityType = "ORDER", idParamIndex = 0)
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
    @AuditAction(action = "ORDER_CREATED", entityType = "ORDER")
    public OrderResponse createOrderResponse(OrderRequest request, Long userId, String idempotencyKey) {
        return databaseRetryExecutor.execute(
                "order-create",
                () -> withWriteTransaction(() -> idempotencyService.execute(
                        idempotencyKey,
                        "ORDER_CREATE",
                        userId,
                        "ORDER",
                        id -> getFullOrderResponseByIdAndUserId(id, userId),
                        () -> createOrderInternal(request, userId, false, null),
                        OrderResponse::getId
                ))
        );
    }

    @Override
    @AuditAction(action = "GUEST_ORDER_CREATED", entityType = "ORDER")
    public OrderResponse createGuestOrderResponse(OrderRequest request, String guestId, String idempotencyKey) {
        if (!guestCheckoutProperties.isEnabled()) {
            throw new UnAuthorizedException("Guest checkout is disabled");
        }
        Long guestUserId = guestCheckoutIdentityService.resolveGuestCheckoutUserId();
        Long guestCartId = cartService.getCartByGuestId(guestId).getId();
        request.setCartId(guestCartId);

        return databaseRetryExecutor.execute(
                "guest-order-create",
                () -> withWriteTransaction(() -> idempotencyService.execute(
                        idempotencyKey,
                        "GUEST_ORDER_CREATE",
                        guestUserId,
                        "ORDER",
                        id -> getGuestFullOrderResponseWithToken(id, guestUserId),
                        () -> createOrderInternal(request, guestUserId, true, guestId),
                        OrderResponse::getId
                ))
        );
    }

    private <T> T withWriteTransaction(Supplier<T> supplier) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return template.execute(status -> supplier.get());
    }

    private OrderResponse createOrderInternal(OrderRequest request, Long userId, boolean guestCheckout, String guestId) {
        Map<String, BigDecimal> pricing = handleVoucherDiscount(request, userId, guestCheckout, guestId);
        User user = userService.getUserById(userId);
        Order order = orderMapper.toEntity(request);
        order.setUser(user);
        order.setCurrency(checkoutPricingProperties.normalizeCurrency(request.getCurrency()));
        Long voucherId = request.getVoucherId();
        if (voucherId != null) {
            Voucher voucher = voucherService.getVoucherById(voucherId);
            order.setVoucher(voucher);
        }
        order.setSubtotalAmount(pricing.get("subtotalAmount"));
        order.setDiscountAmount(pricing.get("discountAmount"));
        order.setShippingFee(pricing.get("shippingFee"));
        order.setTaxAmount(pricing.get("taxAmount"));
        order.setTotalAmount(pricing.get("totalAmount"));
        Order savedOrder = orderRepository.save(order);
        List<OrderItem> orderItems = createOrderItemsService.createOrderItems(savedOrder, request.getCartId());
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId(), userId));
        domainEventPublisher.publish(DomainEventType.ORDER_CREATED, savedOrder.getId(), userId, Map.of());
        sseEmitterService.sendToUser(userId, "order-created", Map.of("orderId", savedOrder.getId()));
        sseEmitterService.sendToAdmins("order-created", Map.of("orderId", savedOrder.getId(), "userId", userId));
        List<OrderItemResponse> orderItemResponses = orderItemMapper.toResponseList(orderItems);
        OrderResponse response = orderMapper.toFullResponse(savedOrder, orderItemResponses);
        if (guestCheckout) {
            response.setGuestAccessToken(guestOrderAccessTokenService.issueToken(savedOrder));
        }
        return response;
    }

    private OrderResponse getGuestFullOrderResponseWithToken(Long orderId, Long guestUserId) {
        OrderResponse response = getFullOrderResponseByIdAndUserId(orderId, guestUserId);
        Order order = getOrderByIdAndUserId(orderId, guestUserId);
        response.setGuestAccessToken(guestOrderAccessTokenService.issueToken(order));
        return response;
    }

    @Override
    public BigDecimal getDisCountAmount(OrderRequest request, Long userId) {
        return handleVoucherDiscount(request, userId, false, null).get("discountAmount");
    }

    private Map<String, BigDecimal> handleVoucherDiscount(OrderRequest request, Long userId, boolean guestCheckout, String guestId) {
        Long cartId = request.getCartId();
        if (cartId == null) {
            throw new NotFoundException("Cart ID is required");
        }
        validateUserAndCart(userId, cartId);
        var cart = cartService.getCartById(cartId);
        if (guestCheckout) {
            if (cart.getGuestId() == null || !cart.getGuestId().equals(guestId)) {
                throw new UnAuthorizedException("Guest cart does not match checkout guest id");
            }
        } else {
            if (cart.getUser() == null || !cart.getUser().getId().equals(userId)) {
                throw new UnAuthorizedException("Cart does not belong to current user");
            }
        }
        List<CartItem> cartItems = cartItemService.getCartItemsByActiveProductTrueAndCartId(cartId, ProductStatus.ACTIVE);
        return calculatePricing(cartItems, request);
    }
}
