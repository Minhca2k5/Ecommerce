package com.minzetsu.ecommerce.order.service;

import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderItem;
import com.minzetsu.ecommerce.order.repository.OrderItemRepository;
import com.minzetsu.ecommerce.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderItemsServiceTest {

    @Mock
    private CartItemService cartItemService;

    @Mock
    private OrderItemRepository orderItemRepository;

    private CreateOrderItemsService service;

    @BeforeEach
    void setUp() {
        service = new CreateOrderItemsService(cartItemService, orderItemRepository);
    }

    @Test
    void createOrderItems_shouldMapSaveAndDeleteCartItems() {
        Order order = Order.builder().build();
        order.setId(10L);

        Product product = Product.builder().name("Phone X").build();
        product.setId(100L);

        CartItem cartItem = CartItem.builder()
                .product(product)
                .quantity(2)
                .unitPriceSnapshot(new BigDecimal("150000"))
                .build();

        when(cartItemService.getCartItemsByCartId(20L)).thenReturn(List.of(cartItem));
        when(orderItemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<OrderItem> result = service.createOrderItems(order, 20L);

        assertThat(result).hasSize(1);
        OrderItem saved = result.get(0);
        assertThat(saved.getOrder()).isSameAs(order);
        assertThat(saved.getProduct()).isSameAs(product);
        assertThat(saved.getProductNameSnapshot()).isEqualTo("Phone X");
        assertThat(saved.getQuantity()).isEqualTo(2);
        assertThat(saved.getUnitPriceSnapshot()).isEqualByComparingTo("150000");
        assertThat(saved.getLineTotal()).isEqualByComparingTo("300000");

        ArgumentCaptor<List<CartItem>> deletedCaptor = ArgumentCaptor.forClass(List.class);
        verify(cartItemService).deleteByCartItems(deletedCaptor.capture());
        assertThat(deletedCaptor.getValue()).containsExactly(cartItem);
    }

    @Test
    void createOrderItems_shouldHandleEmptyCartItems() {
        Order order = Order.builder().build();
        order.setId(11L);

        when(cartItemService.getCartItemsByCartId(21L)).thenReturn(List.of());
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());

        List<OrderItem> result = service.createOrderItems(order, 21L);

        assertThat(result).isEmpty();

        ArgumentCaptor<List<CartItem>> deletedCaptor = ArgumentCaptor.forClass(List.class);
        verify(cartItemService).deleteByCartItems(deletedCaptor.capture());
        assertThat(deletedCaptor.getValue()).isEmpty();
    }
}
