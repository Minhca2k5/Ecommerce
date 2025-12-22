package com.minzetsu.ecommerce.cart.service;

import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.mapper.CartItemMapper;
import com.minzetsu.ecommerce.product.entity.ProductImage;
import com.minzetsu.ecommerce.product.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUrlForCartService {

    private final CartItemMapper cartItemMapper;
    private final ProductImageRepository productImageRepository;

    public CartItemResponse toResponseWithUrl(CartItem cartItem) {
        CartItemResponse response = cartItemMapper.toResponse(cartItem);
        Long productId = response.getProductId();
        Optional<ProductImage> mainImage = productImageRepository.findByIsPrimaryTrueAndProductId(productId);
        mainImage.ifPresent(image -> response.setUrl(image.getUrl()));
        return response;
    }

    public List<CartItemResponse> toResponseListWithUrl(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toResponseWithUrl)
                .toList();
    }

}
