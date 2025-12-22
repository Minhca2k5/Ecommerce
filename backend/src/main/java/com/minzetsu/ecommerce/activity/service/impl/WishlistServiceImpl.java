package com.minzetsu.ecommerce.activity.service.impl;

import com.minzetsu.ecommerce.activity.dto.request.WishlistRequest;
import com.minzetsu.ecommerce.activity.dto.response.WishlistResponse;
import com.minzetsu.ecommerce.activity.entity.Wishlist;
import com.minzetsu.ecommerce.activity.mapper.WishlistMapper;
import com.minzetsu.ecommerce.activity.repository.WishlistRepository;
import com.minzetsu.ecommerce.activity.service.WishlistService;
import com.minzetsu.ecommerce.common.exception.AlreadyExistException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.product.entity.ProductImage;
import com.minzetsu.ecommerce.product.repository.ProductImageRepository;
import com.minzetsu.ecommerce.product.service.ProductService;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistMapper wishlistMapper;
    private final ProductImageRepository productImageRepository;
    private final ProductService productService;
    private final UserService userService;

    private WishlistResponse toResponseWithUrl(Wishlist wishlist) {
        WishlistResponse response = wishlistMapper.toResponse(wishlist);
        Long productId = response.getProductId();
        Optional<ProductImage> mainImage = productImageRepository.findByIsPrimaryTrueAndProductId(productId);
        mainImage.ifPresent(image -> response.setUrl(image.getUrl()));
        return response;
    }

    private List<WishlistResponse> toResponseListWithUrl(List<Wishlist> wishlists) {
        return wishlists.stream()
                .map(this::toResponseWithUrl)
                .toList();
    }

    void existsByUserId(Long userId) {
        if (!wishlistRepository.existsByUserId(userId)) {
            throw new NotFoundException("No wishlists found for user with ID: " + userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WishlistResponse> getWishlistByUserId(Long userId, Pageable pageable) {
        existsByUserId(userId);
        Page<Wishlist> wishlists = wishlistRepository.findByUserId(userId, pageable);
        return wishlists.map(this::toResponseWithUrl);
    }

    @Override
    @Transactional
    public WishlistResponse addProductToWishlist(WishlistRequest request, Long userId) {
        Long productId = request.getProductId();
        if (!productService.existsById(productId)) {
            throw new NotFoundException("Product with ID " + productId + " not found.");
        }
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User with ID " + userId + " not found.");
        }
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new AlreadyExistException("Product with ID " + productId + " is already in the wishlist for user with ID " + userId + ".");
        }
        Wishlist wishlist = wishlistMapper.toEntity(request);
        wishlist.setProduct(productService.getProductById(productId));
        wishlist.setUser(userService.getUserById(userId));
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        return toResponseWithUrl(savedWishlist);
    }

    @Override
    @Transactional
    public void removeProductFromWishlist(Long id, Long userId) {
        if (id == null) {
            throw new NotFoundException("Wishlist item ID cannot be null.");
        }
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Wishlist item with ID " + id + " not found."));
        if (!wishlist.getUser().getId().equals(userId)) {
            throw new NotFoundException("User with ID " + userId + " is not authorized to delete this wishlist item.");
        }
        wishlistRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void clearWishlistByUserId(Long userId) {
        existsByUserId(userId);
        wishlistRepository.deleteByUserId(userId);
    }

    @Override
    public List<WishlistResponse> getWishlistsByProductName(String productName, Long userId) {
        return toResponseListWithUrl(wishlistRepository.findByProductNameByOrderByUpdatedAtDesc(productName, userId));
    }
}
