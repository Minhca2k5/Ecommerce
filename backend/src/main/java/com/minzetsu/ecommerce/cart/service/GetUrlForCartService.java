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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetUrlForCartService {

    private final CartItemMapper cartItemMapper;
    private final ProductImageRepository productImageRepository;

    private Map<Long, String> getPrimaryImageUrlMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productImageRepository.findPrimaryByProductIds(productIds).stream()
                .filter(image -> image.getProduct() != null && image.getProduct().getId() != null)
                .collect(Collectors.toMap(
                        image -> image.getProduct().getId(),
                        ProductImage::getUrl,
                        (existing, ignored) -> existing
                ));
    }

    public CartItemResponse toResponseWithUrl(CartItem cartItem) {
        CartItemResponse response = cartItemMapper.toResponse(cartItem);
        Long productId = response.getProductId();
        Optional<ProductImage> mainImage = productImageRepository.findByIsPrimaryTrueAndProductId(productId);
        mainImage.ifPresent(image -> response.setUrl(image.getUrl()));
        return response;
    }

    public List<CartItemResponse> toResponseListWithUrl(List<CartItem> cartItems) {
        List<CartItemResponse> responses = cartItemMapper.toResponseList(cartItems);
        List<Long> productIds = responses.stream()
                .map(CartItemResponse::getProductId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, String> urlMap = getPrimaryImageUrlMap(productIds);
        responses.forEach(response -> response.setUrl(urlMap.get(response.getProductId())));
        return responses;
    }

}
