package com.minzetsu.ecommerce.cart.service.impl;

import com.minzetsu.ecommerce.cart.dto.request.CartItemRequest;
import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;
import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.mapper.CartItemMapper;
import com.minzetsu.ecommerce.cart.repository.CartItemRepository;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.common.exception.*;
import com.minzetsu.ecommerce.inventory.service.InventoryService;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.entity.ProductImage;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.product.repository.ProductImageRepository;
import com.minzetsu.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final ProductService productService;
    private final CartService cartService;
    private final InventoryService inventoryService;
    private final ProductImageRepository productImageRepository;

    private CartItemResponse toResponseWithUrl(CartItem cartItem) {
        CartItemResponse response = cartItemMapper.toResponse(cartItem);
        Long productId = response.getId();
        Optional<ProductImage> mainImage = productImageRepository.findByIsPrimaryTrueAndProductId(productId);
        mainImage.ifPresent(image -> response.setUrl(image.getUrl()));
        return response;
    }

    private List<CartItemResponse> toResponseListWithUrl(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toResponseWithUrl)
                .toList();
    }

    private void validateCartAndProduct(Long cartId, Long productId) {
        if (!cartService.existsById(cartId)) {
            throw new NotFoundException("Cart not found with id: " + cartId);
        }
        if (!productService.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
    }

    private int getAvailableStock(Long productId) {
        int totalStock = inventoryService.getTotalStockQuantityByProductId(productId);
        int reservedStock = inventoryService.getTotalReservedQuantityByProductId(productId);
        return totalStock - reservedStock;
    }

    private void validateStock(Long productId, int requiredQty) {
        int available = getAvailableStock(productId);
        if (requiredQty > available) {
            throw new InsufficientNumberException("Insufficient stock for product id: " + productId +
                    ". Available: " + available + ", Required: " + requiredQty);
        }
    }

    private Cart resolveCart(Long cartId, Long userId) {
        if (cartId != null) {
            return cartService.getCartById(cartId);
        }
        if (userId != null) {
            return cartService.getCartByUserId(userId);
        }
        throw new InvalidObjectException("Either cartId or userId must be provided");
    }

    private <T> T findOrThrow(Supplier<T> supplier, String message) {
        T value = supplier.get();
        if (value == null) throw new NotFoundException(message);
        return value;
    }

    @Override
    public boolean existsByCartId(Long cartId) {
        return cartItemRepository.existsByCartId(cartId);
    }

    @Override
    public boolean existsById(Long id) {
        return cartItemRepository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteByCartId(Long cartId) {
        List<CartItem> cartItems = findOrThrow(() -> getCartItemsByCartId(cartId),
                "No CartItems found for cartId: " + cartId);
        cartItems.forEach(cartItem -> deleteById(cartItem.getId()));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        CartItem cartItem = getCartItemById(id);
        inventoryService.updateQuantityByCartItemAmountReturned(cartItem.getProduct().getId(), cartItem.getQuantity());
        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        if (existsByCartId(cartId)) {
            throw new NotFoundException("No CartItems found for cartId: " + cartId);
        }
        return cartItemRepository.findByCartId(cartId);
    }

    @Override
    public List<CartItem> getCartItemsByActiveProductTrueAndCartId(Long cartId, ProductStatus status) {
        return cartItemRepository.findByActiveProductTrueAndCartId(cartId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CartItem> getCartItemsByCartId(Long cartId, Pageable pageable) {
        if (existsByCartId(cartId)) {
            throw new NotFoundException("No CartItems found for cartId: " + cartId);
        }
        return cartItemRepository.findByCartId(cartId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CartItem getCartItemByIdAndUserId(Long id, Long userId) {
        CartItem cartItem = getCartItemById(id);
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("You are not authorized to view this cart item");
        }
        return cartItem;
    }

    @Override
    @Transactional(readOnly = true)
    public CartItem getCartItemById(Long id) {
        return cartItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CartItem not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CartItemResponse> getCartItemResponsesByCartId(Long cartId, Long userId, Pageable pageable) {
        Cart cart = resolveCart(cartId, userId);
        Page<CartItem> cartItemsPage = getCartItemsByCartId(cart.getId(), pageable);
        return cartItemsPage.map(this::toResponseWithUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItemResponsesByCartId(Long cartId, Long userId) {
        Cart cart = resolveCart(cartId, userId);
        List<CartItem> cartItems = getCartItemsByCartId(cart.getId());
        return toResponseListWithUrl(cartItems);
    }

    @Override
    @Transactional(readOnly = true)
    public CartItemResponse getCartItemResponseByIdAndUserId(Long id, Long userId) {
        CartItem cartItem = (userId != null)
                ? getCartItemByIdAndUserId(id, userId)
                : getCartItemById(id);
        return toResponseWithUrl(cartItem);
    }

    @Override
    @Transactional
    public CartItemResponse addOrUpdateCartItemResponse(CartItemRequest request, boolean isReturned, Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        Long cartId = cart.getId();
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        validateCartAndProduct(cartId, productId);
        if (!isReturned) validateStock(productId, quantity);

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .map(existingItem -> updateExistingItem(existingItem, quantity, isReturned, productId))
                .orElseGet(() -> createNewCartItem(request, cartId, productId, quantity));

        return toResponseWithUrl(cartItem);
    }

    private CartItem updateExistingItem(CartItem existingItem, int quantity, boolean isReturned, Long productId) {
        if (isReturned) {
            cartItemRepository.updateQuantityById(quantity, existingItem.getId(), true);
            inventoryService.updateQuantityByCartItemAmountReturned(productId, quantity);
        } else {
            cartItemRepository.updateQuantityById(quantity, existingItem.getId(), false);
            inventoryService.updateQuantityByCartItemAmountBorrowed(productId, quantity);
        }
        return getCartItemById(existingItem.getId());
    }

    private CartItem createNewCartItem(CartItemRequest request, Long cartId, Long productId, int quantity) {
        inventoryService.updateQuantityByCartItemAmountBorrowed(productId, quantity);
        CartItem cartItem = cartItemMapper.toEntity(request);
        cartItem.setCart(cartService.getCartById(cartId));
        Product product = productService.getProductById(productId);
        cartItem.setProduct(product);
        cartItem.setUnitPriceSnapshot(product.getPrice());
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCarItemResponsesByProductName(String productName, Long userId) {
        return toResponseListWithUrl(
                cartItemRepository.findByProductName(productName, userId)
        );
    }
}
