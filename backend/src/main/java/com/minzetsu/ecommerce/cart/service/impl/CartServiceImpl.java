package com.minzetsu.ecommerce.cart.service.impl;

import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;
import com.minzetsu.ecommerce.cart.mapper.CartMapper;
import com.minzetsu.ecommerce.cart.repository.CartItemRepository;
import com.minzetsu.ecommerce.cart.repository.CartRepository;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.cart.service.GetUrlForCartService;
import com.minzetsu.ecommerce.common.audit.entity.AuditAction;
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
import java.util.UUID;

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
    public boolean existsByGuestId(String guestId) {
        return cartRepository.existsByGuestId(guestId);
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
    public Cart getCartByGuestId(String guestId) {
        return findCart(cartRepository.findByGuestId(guestId),
                "Guest cart not found for guestId: " + guestId);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getFullCartResponseByUserId(Long userId) {
        return buildFullCartResponse(getCartByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getFullCartResponseByGuestId(String guestId) {
        return buildFullCartResponse(getCartByGuestId(guestId));
    }

    @Override
    @Transactional
    @AuditAction(action = "CART_CREATED", entityType = "CART")
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

    @Override
    @Transactional
    @AuditAction(action = "GUEST_CART_CREATED", entityType = "CART")
    public CartResponse createGuestCartResponse(String guestId) {
        String resolvedGuestId = (guestId == null || guestId.isBlank())
                ? UUID.randomUUID().toString()
                : guestId;
        if (existsByGuestId(resolvedGuestId)) {
            return cartMapper.toResponse(getCartByGuestId(resolvedGuestId));
        }
        Cart cart = new Cart();
        cart.setGuestId(resolvedGuestId);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    @AuditAction(action = "GUEST_CART_MERGED", entityType = "CART")
    public CartResponse mergeGuestCartToUser(String guestId, Long userId) {
        validateUser(userId);
        if (guestId == null || guestId.isBlank() || !existsByGuestId(guestId)) {
            return getFullCartResponseByUserId(userId);
        }

        Cart guestCart = getCartByGuestId(guestId);
        Cart userCart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userService.getUserById(userId);
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });

        List<com.minzetsu.ecommerce.cart.entity.CartItem> guestItems =
                cartItemRepository.findByCartIdOrderByUpdatedAtDesc(guestCart.getId());

        for (com.minzetsu.ecommerce.cart.entity.CartItem guestItem : guestItems) {
            Long productId = guestItem.getProduct().getId();
            Optional<com.minzetsu.ecommerce.cart.entity.CartItem> existing =
                    cartItemRepository.findByCartIdAndProductId(userCart.getId(), productId);

            if (existing.isPresent()) {
                com.minzetsu.ecommerce.cart.entity.CartItem userItem = existing.get();
                int mergedQty = userItem.getQuantity() + guestItem.getQuantity();
                userItem.setQuantity(mergedQty);
                cartItemRepository.save(userItem);
                cartItemRepository.delete(guestItem);
            } else {
                guestItem.setCart(userCart);
                cartItemRepository.save(guestItem);
            }
        }

        cartRepository.delete(guestCart);
        return buildFullCartResponse(userCart);
    }
}

