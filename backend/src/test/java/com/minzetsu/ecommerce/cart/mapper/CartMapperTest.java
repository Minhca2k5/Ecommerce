package com.minzetsu.ecommerce.cart.mapper;

import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;
import com.minzetsu.ecommerce.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartMapperTest {

    private final CartMapper cartMapper = Mappers.getMapper(CartMapper.class);

    @Test
    void toFullResponse_shouldCalculateLineTotalSubtotalAndTotal() {
        User user = User.builder()
                .username("user1")
                .email("user1@test.com")
                .password("secret")
                .fullName("User One")
                .build();
        user.setId(10L);

        Cart cart = Cart.builder()
                .user(user)
                .guestId(null)
                .build();

        CartItemResponse first = CartItemResponse.builder()
                .productId(1L)
                .quantity(2)
                .unitPriceSnapshot(new BigDecimal("50000"))
                .lineTotal(null)
                .build();

        CartItemResponse second = CartItemResponse.builder()
                .productId(2L)
                .quantity(1)
                .unitPriceSnapshot(new BigDecimal("20000"))
                .lineTotal(new BigDecimal("20000"))
                .build();

        CartResponse result = cartMapper.toFullResponse(
                cart,
                List.of(first, second),
                new BigDecimal("10000"),
                new BigDecimal("15000"),
                "VND"
        );

        assertThat(first.getLineTotal()).isEqualByComparingTo("100000");
        assertThat(result.getItemCount()).isEqualTo(2);
        assertThat(result.getTotalQuantity()).isEqualTo(3);
        assertThat(result.getItemsSubtotal()).isEqualByComparingTo("120000");
        assertThat(result.getDiscount()).isEqualByComparingTo("10000");
        assertThat(result.getShippingFee()).isEqualByComparingTo("15000");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("125000");
        assertThat(result.getCurrency()).isEqualTo("VND");
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getUsername()).isEqualTo("user1");
    }

    @Test
    void toFullResponse_shouldUseZeroForNullDiscountShippingAndDefaultCurrency() {
        Cart cart = Cart.builder().guestId("guest-abc").build();

        CartItemResponse item = CartItemResponse.builder()
                .quantity(3)
                .unitPriceSnapshot(new BigDecimal("10000"))
                .lineTotal(null)
                .build();

        CartResponse result = cartMapper.toFullResponse(cart, List.of(item), null, null, null);

        assertThat(result.getItemsSubtotal()).isEqualByComparingTo("30000");
        assertThat(result.getDiscount()).isEqualByComparingTo("0");
        assertThat(result.getShippingFee()).isEqualByComparingTo("0");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("30000");
        assertThat(result.getCurrency()).isEqualTo("VND");
        assertThat(result.getGuestId()).isEqualTo("guest-abc");
    }

    @Test
    void toFullResponse_shouldHandleNullItemsAsEmptyCart() {
        Cart cart = Cart.builder().guestId("guest-empty").build();

        CartResponse result = cartMapper.toFullResponse(
                cart,
                null,
                new BigDecimal("5000"),
                null,
                "USD"
        );

        assertThat(result.getItemCount()).isEqualTo(0);
        assertThat(result.getTotalQuantity()).isEqualTo(0);
        assertThat(result.getItemsSubtotal()).isEqualByComparingTo("0");
        assertThat(result.getDiscount()).isEqualByComparingTo("5000");
        assertThat(result.getShippingFee()).isEqualByComparingTo("0");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("-5000");
        assertThat(result.getCurrency()).isEqualTo("USD");
    }

    @Test
    void toFullResponse_shouldTreatNullQuantityAndUnitPriceAsZero() {
        Cart cart = Cart.builder().guestId("guest-null-item").build();

        CartItemResponse item = CartItemResponse.builder()
                .quantity(null)
                .unitPriceSnapshot(null)
                .lineTotal(null)
                .build();

        CartResponse result = cartMapper.toFullResponse(cart, List.of(item), null, null, "VND");

        assertThat(item.getLineTotal()).isEqualByComparingTo("0");
        assertThat(result.getTotalQuantity()).isEqualTo(0);
        assertThat(result.getItemsSubtotal()).isEqualByComparingTo("0");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("0");
    }
}
