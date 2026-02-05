package com.minzetsu.ecommerce.cart.controller.pub;

import com.minzetsu.ecommerce.cart.dto.request.CartItemRequest;
import com.minzetsu.ecommerce.cart.dto.request.GuestCartItemRequest;
import com.minzetsu.ecommerce.cart.dto.request.GuestCartRequest;
import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;
import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.repository.CartItemRepository;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/carts/guest")
@RequiredArgsConstructor
@Tag(name = "Public - Guest Cart", description = "Guest cart APIs (no login required)")
public class PublicGuestCartController {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final CartItemRepository cartItemRepository;

    @Operation(summary = "Create or reuse a guest cart")
    @PostMapping
    public ResponseEntity<CartResponse> createGuestCart(@RequestBody(required = false) GuestCartRequest request) {
        String guestId = request != null ? request.getGuestId() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.createGuestCartResponse(guestId));
    }

    @Operation(summary = "Get guest cart by guestId")
    @GetMapping("/{guestId}")
    public ResponseEntity<CartResponse> getGuestCart(@PathVariable String guestId) {
        return ResponseEntity.ok(cartService.getFullCartResponseByGuestId(guestId));
    }

    @Operation(summary = "List guest cart items")
    @GetMapping("/{guestId}/items")
    public ResponseEntity<List<CartItemResponse>> getGuestCartItems(@PathVariable String guestId) {
        Cart cart = cartService.getCartByGuestId(guestId);
        return ResponseEntity.ok(cartItemService.getCartItemResponsesByCartId(cart.getId(), null));
    }

    @Operation(summary = "Add or update guest cart item")
    @PostMapping("/{guestId}/items")
    public ResponseEntity<CartItemResponse> addOrUpdateGuestCartItem(
            @PathVariable String guestId,
            @Valid @RequestBody GuestCartItemRequest guestRequest
    ) {
        Cart cart = cartService.getCartByGuestId(guestId);
        CartItemRequest request = CartItemRequest.builder()
                .cartId(cart.getId())
                .productId(guestRequest.getProductId())
                .quantity(guestRequest.getQuantity())
                .build();
        CartItemResponse response = cartItemService.addOrUpdateCartItemResponseByCartId(request, false, cart.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Remove item from guest cart")
    @DeleteMapping("/{guestId}/items/{cartItemId}")
    public ResponseEntity<Void> deleteGuestCartItem(
            @PathVariable String guestId,
            @PathVariable Long cartItemId
    ) {
        Cart cart = cartService.getCartByGuestId(guestId);
        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new NotFoundException("CartItem not found with id: " + cartItemId));
        cartItemService.deleteByCartItem(cartItem);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear guest cart")
    @DeleteMapping("/{guestId}/items")
    public ResponseEntity<Void> clearGuestCart(@PathVariable String guestId) {
        Cart cart = cartService.getCartByGuestId(guestId);
        cartItemService.deleteByCartId(cart.getId());
        return ResponseEntity.noContent().build();
    }
}
