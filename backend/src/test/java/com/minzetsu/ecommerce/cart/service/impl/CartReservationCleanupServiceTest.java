package com.minzetsu.ecommerce.cart.service.impl;

import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.repository.CartItemRepository;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartReservationCleanupServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartItemService cartItemService;

    @InjectMocks
    private CartReservationCleanupService cleanupService;

    @Test
    void releaseExpiredReservations_shouldDoNothingWhenNoExpiredItems() {
        ReflectionTestUtils.setField(cleanupService, "reservationTtlMinutes", 30L);
        when(cartItemRepository.findByUpdatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        cleanupService.releaseExpiredReservations();

        verify(cartItemRepository, times(1)).findByUpdatedAtBefore(any(LocalDateTime.class));
        verify(cartItemService, never()).deleteByCartItem(any(CartItem.class));
    }

    @Test
    void releaseExpiredReservations_shouldDeleteEachExpiredItem() {
        ReflectionTestUtils.setField(cleanupService, "reservationTtlMinutes", 30L);

        CartItem first = CartItem.builder().quantity(1).build();
        CartItem second = CartItem.builder().quantity(2).build();

        when(cartItemRepository.findByUpdatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(first, second));

        cleanupService.releaseExpiredReservations();

        verify(cartItemRepository, times(1)).findByUpdatedAtBefore(any(LocalDateTime.class));
        verify(cartItemService, times(1)).deleteByCartItem(first);
        verify(cartItemService, times(1)).deleteByCartItem(second);
    }
}
