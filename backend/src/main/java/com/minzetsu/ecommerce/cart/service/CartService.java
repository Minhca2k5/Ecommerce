package com.minzetsu.ecommerce.cart.service;

import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;

public interface CartService {
    boolean existsByUserId(Long userId);
    boolean existsById(Long id);
    boolean existsByGuestId(String guestId);
    Cart getCartByUserId(Long userId);
    Cart getCartById(Long cartId);
    Cart getCartByGuestId(String guestId);

    CartResponse getFullCartResponseByUserId(Long userId);
    CartResponse getFullCartResponseByGuestId(String guestId);
    CartResponse createCartResponse(Long userId);
    CartResponse createGuestCartResponse(String guestId);
    CartResponse mergeGuestCartToUser(String guestId, Long userId);
}
