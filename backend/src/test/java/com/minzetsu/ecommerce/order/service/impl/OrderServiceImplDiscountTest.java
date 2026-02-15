package com.minzetsu.ecommerce.order.service.impl;

import com.minzetsu.ecommerce.cart.entity.Cart;
import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.common.idempotency.IdempotencyService;
import com.minzetsu.ecommerce.common.exception.AppException;
import com.minzetsu.ecommerce.common.utils.DatabaseRetryExecutor;
import com.minzetsu.ecommerce.messaging.DomainEventPublisher;
import com.minzetsu.ecommerce.mongo.ClickstreamEventService;
import com.minzetsu.ecommerce.order.config.CheckoutPricingProperties;
import com.minzetsu.ecommerce.order.config.GuestCheckoutProperties;
import com.minzetsu.ecommerce.order.dto.request.OrderRequest;
import com.minzetsu.ecommerce.order.mapper.OrderItemMapper;
import com.minzetsu.ecommerce.order.mapper.OrderMapper;
import com.minzetsu.ecommerce.order.repository.OrderItemRepository;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import com.minzetsu.ecommerce.order.service.CreateOrderItemsService;
import com.minzetsu.ecommerce.order.service.GuestCheckoutIdentityService;
import com.minzetsu.ecommerce.order.service.GuestOrderAccessTokenService;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import com.minzetsu.ecommerce.promotion.entity.VoucherDiscountType;
import com.minzetsu.ecommerce.promotion.service.VoucherService;
import com.minzetsu.ecommerce.realtime.SseEmitterService;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplDiscountTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserService userService;
    @Mock
    private CartService cartService;
    @Mock
    private VoucherService voucherService;
    @Mock
    private CartItemService cartItemService;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CreateOrderItemsService createOrderItemsService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private DomainEventPublisher domainEventPublisher;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private SseEmitterService sseEmitterService;
    @Mock
    private DatabaseRetryExecutor databaseRetryExecutor;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private ClickstreamEventService clickstreamEventService;
    @Mock
    private GuestCheckoutIdentityService guestCheckoutIdentityService;
    @Mock
    private GuestOrderAccessTokenService guestOrderAccessTokenService;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository,
                orderMapper,
                userService,
                cartService,
                voucherService,
                cartItemService,
                orderItemMapper,
                orderItemRepository,
                createOrderItemsService,
                eventPublisher,
                domainEventPublisher,
                idempotencyService,
                sseEmitterService,
                databaseRetryExecutor,
                transactionManager,
                clickstreamEventService,
                guestCheckoutIdentityService,
                guestOrderAccessTokenService,
                new GuestCheckoutProperties(),
                new CheckoutPricingProperties()
        );
    }

    @Test
    void getDisCountAmount_shouldApplyFixedDiscountAndCapBySubtotal() {
        long userId = 10L;
        long cartId = 20L;
        long voucherId = 30L;

        mockValidCartContext(userId, cartId, new BigDecimal("100000"));

        Voucher fixedVoucher = Voucher.builder()
                .discountType(VoucherDiscountType.FIXED)
                .discountValue(new BigDecimal("150000"))
                .build();
        when(voucherService.getVoucherById(voucherId)).thenReturn(fixedVoucher);

        OrderRequest request = OrderRequest.builder()
                .cartId(cartId)
                .voucherId(voucherId)
                .currency("VND")
                .build();

        BigDecimal discount = orderService.getDisCountAmount(request, userId);

        assertThat(discount).isEqualByComparingTo("100000.00");
    }

    @Test
    void getDisCountAmount_shouldApplyPercentDiscountWithMaxCap() {
        long userId = 11L;
        long cartId = 21L;
        long voucherId = 31L;

        mockValidCartContext(userId, cartId, new BigDecimal("100000"));

        Voucher percentVoucher = Voucher.builder()
                .discountType(VoucherDiscountType.PERCENT)
                .discountValue(new BigDecimal("50"))
                .maxDiscountAmount(new BigDecimal("30000"))
                .build();
        when(voucherService.getVoucherById(voucherId)).thenReturn(percentVoucher);

        OrderRequest request = OrderRequest.builder()
                .cartId(cartId)
                .voucherId(voucherId)
                .currency("VND")
                .build();

        BigDecimal discount = orderService.getDisCountAmount(request, userId);

        assertThat(discount).isEqualByComparingTo("30000.00");
    }

    @Test
    void getDisCountAmount_shouldApplyFreeShipDiscountFromShippingFee() {
        long userId = 12L;
        long cartId = 22L;
        long voucherId = 32L;

        mockValidCartContext(userId, cartId, new BigDecimal("100000"));

        Voucher freeShipVoucher = Voucher.builder()
                .discountType(VoucherDiscountType.FREESHIP)
                .build();
        when(voucherService.getVoucherById(voucherId)).thenReturn(freeShipVoucher);

        OrderRequest request = OrderRequest.builder()
                .cartId(cartId)
                .voucherId(voucherId)
                .shippingFee(new BigDecimal("25000"))
                .currency("VND")
                .build();

        BigDecimal discount = orderService.getDisCountAmount(request, userId);

        assertThat(discount).isEqualByComparingTo("25000.00");
    }

    @Test
    void getDisCountAmount_shouldReturnZeroWhenNoVoucherProvided() {
        long userId = 13L;
        long cartId = 23L;

        mockValidCartContext(userId, cartId, new BigDecimal("100000"));

        OrderRequest request = OrderRequest.builder()
                .cartId(cartId)
                .currency("VND")
                .build();

        BigDecimal discount = orderService.getDisCountAmount(request, userId);

        assertThat(discount).isEqualByComparingTo("0");
    }

    @Test
    void getDisCountAmount_shouldThrowBadRequestForUnsupportedCurrency() {
        long userId = 14L;
        long cartId = 24L;

        mockValidCartContext(userId, cartId, new BigDecimal("100000"));

        OrderRequest request = OrderRequest.builder()
                .cartId(cartId)
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> orderService.getDisCountAmount(request, userId))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Unsupported currency")
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void mockValidCartContext(long userId, long cartId, BigDecimal itemUnitPrice) {
        when(userService.existsById(userId)).thenReturn(true);
        when(cartService.existsById(cartId)).thenReturn(true);

        User user = User.builder()
                .username("u" + userId)
                .email("u" + userId + "@test.com")
                .password("pw")
                .build();
        user.setId(userId);

        Cart cart = Cart.builder().user(user).build();
        when(cartService.getCartById(cartId)).thenReturn(cart);

        CartItem cartItem = CartItem.builder()
                .quantity(1)
                .unitPriceSnapshot(itemUnitPrice)
                .build();

        when(cartItemService.getCartItemsByActiveProductTrueAndCartId(eq(cartId), eq(ProductStatus.ACTIVE)))
                .thenReturn(List.of(cartItem));

    }
}
