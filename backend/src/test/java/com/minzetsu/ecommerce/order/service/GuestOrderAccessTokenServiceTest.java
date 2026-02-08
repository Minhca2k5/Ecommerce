package com.minzetsu.ecommerce.order.service;

import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.order.config.GuestCheckoutProperties;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderStatus;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import com.minzetsu.ecommerce.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestOrderAccessTokenServiceTest {

    @Mock
    private GuestCheckoutIdentityService guestCheckoutIdentityService;

    @Mock
    private OrderRepository orderRepository;

    private GuestCheckoutProperties properties;
    private GuestOrderAccessTokenService tokenService;

    @BeforeEach
    void setUp() {
        properties = new GuestCheckoutProperties();
        properties.setAccessTokenSecret("test-secret-123");
        properties.setAccessTokenTtlMinutes(60);

        tokenService = new GuestOrderAccessTokenService(properties, guestCheckoutIdentityService, orderRepository);
    }

    @Test
    void authorizeGuestOrder_shouldReturnOrderWhenTokenIsValid() {
        User guestUser = User.builder().username("guest_checkout").email("g@test.com").password("pw").build();
        guestUser.setId(999L);

        Order order = Order.builder()
                .user(guestUser)
                .addressIdSnapshot(1L)
                .currency("VND")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .build();
        order.setId(100L);
        order.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        String token = tokenService.issueToken(order);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(guestCheckoutIdentityService.resolveGuestCheckoutUserId()).thenReturn(999L);

        Order authorized = tokenService.authorizeGuestOrder(100L, token);

        assertThat(authorized.getId()).isEqualTo(100L);
    }

    @Test
    void authorizeGuestOrder_shouldRejectWhenOrderIdDoesNotMatchToken() {
        User guestUser = User.builder().username("guest_checkout").email("g@test.com").password("pw").build();
        guestUser.setId(999L);

        Order order = Order.builder()
                .user(guestUser)
                .addressIdSnapshot(1L)
                .currency("VND")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .build();
        order.setId(200L);
        order.setCreatedAt(LocalDateTime.now());

        String token = tokenService.issueToken(order);

        assertThatThrownBy(() -> tokenService.authorizeGuestOrder(201L, token))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessageContaining("does not match order");
    }

    @Test
    void authorizeGuestOrder_shouldRejectExpiredToken() {
        User guestUser = User.builder().username("guest_checkout").email("g@test.com").password("pw").build();
        guestUser.setId(999L);

        properties.setAccessTokenTtlMinutes(1);

        Order order = Order.builder()
                .user(guestUser)
                .addressIdSnapshot(1L)
                .currency("VND")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .build();
        order.setId(300L);
        order.setCreatedAt(LocalDateTime.now().minusHours(10));

        String token = tokenService.issueToken(order);

        assertThatThrownBy(() -> tokenService.authorizeGuestOrder(300L, token))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessageContaining("expired");
    }
}
