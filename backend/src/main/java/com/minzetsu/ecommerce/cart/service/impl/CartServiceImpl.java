package com.minzetsu.ecommerce.cart.service.impl;

import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;
import com.minzetsu.ecommerce.cart.mapper.CartMapper;
import com.minzetsu.ecommerce.cart.repository.CartItemRepository;
import com.minzetsu.ecommerce.cart.repository.CartRepository;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.cart.service.GetUrlForCartService;
import com.minzetsu.ecommerce.common.exception.AlreadyExistException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final UserService userService;
    private final CartItemRepository cartItemRepository;
    private final GetUrlForCartService getUrlForCartService;

    private void validateUser(Long userId) {
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
    }

    private Cart findCart(Optional<Cart> optionalCart, String message) {
        return optionalCart.orElseThrow(() -> new NotFoundException(message));
    }

    private CartResponse buildFullCartResponse(Cart cart) {
        List<CartItemResponse> cartItems = getUrlForCartService.toResponseListWithUrl(
                cartItemRepository.findByCartIdOrderByUpdatedAtDesc(cart.getId())
        );
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal shippingFee = BigDecimal.ZERO;
        return cartMapper.toFullResponse(cart, cartItems, discount, shippingFee, null);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return cartRepository.existsByUserId(userId);
    }

    @Override
    public boolean existsById(Long id) {
        return cartRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        return findCart(cartRepository.findByUserId(userId),
                "Cart not found for userId: " + userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCartById(Long cartId) {
        return findCart(cartRepository.findById(cartId),
                "Cart not found with id: " + cartId);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getFullCartResponseByUserId(Long userId) {
        return buildFullCartResponse(getCartByUserId(userId));
    }

    @Override
    @Transactional
    public CartResponse createCartResponse(Long userId) {
        validateUser(userId);
        if (existsByUserId(userId)) {
            throw new AlreadyExistException("Cart already exists for userId: " + userId);
        }
        User user = userService.getUserById(userId);
        Cart cart = new Cart();
        cart.setUser(user);
        return cartMapper.toResponse(cartRepository.save(cart));
    }
}
