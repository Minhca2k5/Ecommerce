package com.minzetsu.ecommerce.cart.service.impl;

import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.repository.CartItemRepository;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartReservationCleanupService {

    private final CartItemRepository cartItemRepository;
    private final CartItemService cartItemService;

    @Value("${cart.reservation-ttl-minutes:30}")
    private long reservationTtlMinutes;

    @Scheduled(fixedDelayString = "${cart.reservation-cleanup-interval-ms:60000}")
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(reservationTtlMinutes);
        List<CartItem> expiredItems = cartItemRepository.findByUpdatedAtBefore(cutoff);
        if (expiredItems.isEmpty()) {
            return;
        }
        log.info("Releasing {} expired cart reservations (cutoff={})", expiredItems.size(), cutoff);
        for (CartItem item : expiredItems) {
            cartItemService.deleteByCartItem(item);
        }
    }
}
