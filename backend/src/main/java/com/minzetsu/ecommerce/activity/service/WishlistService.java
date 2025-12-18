package com.minzetsu.ecommerce.activity.service;

import com.minzetsu.ecommerce.activity.dto.request.WishlistRequest;
import com.minzetsu.ecommerce.activity.dto.response.WishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WishlistService {
    Page<WishlistResponse> getWishlistByUserId(Long userId, Pageable pageable);
    WishlistResponse addProductToWishlist(WishlistRequest request, Long userId);
    void removeProductFromWishlist(Long id, Long userId);
    void clearWishlistByUserId(Long userId);
    List<WishlistResponse> getWishlistsByProductName(String productName, Long userId);
}
