package com.minzetsu.ecommerce.cart.service;

import com.minzetsu.ecommerce.cart.dto.request.CartItemRequest;
import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CartItemService {
    boolean existsByCartId(Long cartId);
    boolean existsById(Long id);
    void deleteByCartId(Long cartId);
    void deleteById(Long id);
    List<CartItem> getCartItemsByCartId(Long cartId);
    List<CartItem> getCartItemsByActiveProductTrueAndCartId(Long cartId, ProductStatus status);
    Page<CartItem> getCartItemsByCartId(Long cartId, Pageable pageable);
    CartItem getCartItemByIdAndUserId(Long id, Long userId);
    CartItem getCartItemById(Long id);

    Page<CartItemResponse> getCartItemResponsesByCartId(Long cartId, Long userId, Pageable pageable);
    List<CartItemResponse> getCartItemResponsesByCartId(Long cartId, Long userId);
    CartItemResponse getCartItemResponseByIdAndUserId(Long id, Long userId);
    CartItemResponse addOrUpdateCartItemResponse(CartItemRequest request, boolean isReturned, Long userId);
    List<CartItemResponse> getCarItemResponsesByProductName(String productName, Long userId);
}
