package com.minzetsu.ecommerce.cart.service;

import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;

public interface CartService {
    boolean existsByUserId(Long userId);
    boolean existsById(Long id);
    Cart getCartByUserId(Long userId);
    Cart getCartById(Long cartId);

    CartResponse getFullCartResponseByUserId(Long userId);
    CartResponse createCartResponse(Long userId);
}
