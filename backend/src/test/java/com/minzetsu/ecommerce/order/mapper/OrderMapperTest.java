package com.minzetsu.ecommerce.order.mapper;

import com.minzetsu.ecommerce.order.dto.response.OrderItemResponse;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderStatus;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import com.minzetsu.ecommerce.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void toFullResponse_shouldMapOrderAndSetItemCount() {
        User user = User.builder()
                .username("user-a")
                .email("user-a@test.com")
                .password("pw")
                .fullName("User A")
                .build();
        user.setId(100L);

        Voucher voucher = Voucher.builder().code("VC1").name("Voucher 1").build();
        voucher.setId(200L);

        Order order = Order.builder()
                .user(user)
                .voucher(voucher)
                .addressIdSnapshot(10L)
                .currency("VND")
                .status(OrderStatus.PENDING)
                .subtotalAmount(new BigDecimal("120000"))
                .discountAmount(new BigDecimal("10000"))
                .shippingFee(new BigDecimal("15000"))
                .taxAmount(new BigDecimal("10000"))
                .totalAmount(new BigDecimal("135000"))
                .build();

        OrderItemResponse item1 = OrderItemResponse.builder().quantity(2).lineTotal(new BigDecimal("100000")).build();
        OrderItemResponse item2 = OrderItemResponse.builder().quantity(1).lineTotal(new BigDecimal("20000")).build();

        OrderResponse response = orderMapper.toFullResponse(order, List.of(item1, item2));

        assertThat(response.getUserId()).isEqualTo(100L);
        assertThat(response.getVoucherId()).isEqualTo(200L);
        assertThat(response.getUsername()).isEqualTo("user-a");
        assertThat(response.getFullName()).isEqualTo("User A");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getSubtotalAmount()).isEqualByComparingTo("120000");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("10000");
        assertThat(response.getShippingFee()).isEqualByComparingTo("15000");
        assertThat(response.getTaxAmount()).isEqualByComparingTo("10000");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("135000");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItemCount()).isEqualTo(2);
    }

    @Test
    void toFullResponse_shouldHandleNullItems() {
        User user = User.builder()
                .username("user-b")
                .email("user-b@test.com")
                .password("pw")
                .build();
        user.setId(101L);

        Order order = Order.builder()
                .user(user)
                .addressIdSnapshot(11L)
                .currency("USD")
                .status(OrderStatus.PAID)
                .totalAmount(new BigDecimal("99.99"))
                .build();

        OrderResponse response = orderMapper.toFullResponse(order, null);

        assertThat(response.getUserId()).isEqualTo(101L);
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getStatus()).isEqualTo("PAID");
        assertThat(response.getItems()).isNull();
        assertThat(response.getItemCount()).isEqualTo(0);
    }

    @Test
    void toFullResponse_shouldKeepVoucherIdNullWhenOrderHasNoVoucher() {
        User user = User.builder()
                .username("user-c")
                .email("user-c@test.com")
                .password("pw")
                .build();
        user.setId(102L);

        Order order = Order.builder()
                .user(user)
                .voucher(null)
                .currency("VND")
                .status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("0"))
                .build();

        OrderResponse response = orderMapper.toFullResponse(order, List.of());

        assertThat(response.getVoucherId()).isNull();
        assertThat(response.getItemCount()).isEqualTo(0);
        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }
}
