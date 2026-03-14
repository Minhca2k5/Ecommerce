package com.minzetsu.ecommerce.cart.service.impl;

import com.minzetsu.ecommerce.cart.dto.request.CartItemRequest;
import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;
import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.mapper.CartItemMapper;
import com.minzetsu.ecommerce.cart.repository.CartItemRepository;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.cart.service.GetUrlForCartService;
import com.minzetsu.ecommerce.common.audit.entity.AuditAction;
import com.minzetsu.ecommerce.inventory.service.InventoryService;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.product.service.ProductService;
import com.minzetsu.ecommerce.common.exception.InsufficientNumberException;
import com.minzetsu.ecommerce.common.exception.InvalidObjectException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.mongo.service.ClickstreamEventService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private static final Logger logger = LoggerFactory.getLogger(CartItemServiceImpl.class);

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final ProductService productService;
    private final CartService cartService;
    private final InventoryService inventoryService;
    private final GetUrlForCartService getUrlForCartService;
    private final ClickstreamEventService clickstreamEventService;


    private void validateCartAndProduct(Long cartId, Long productId) {
        if (!cartService.existsById(cartId)) {
            throw new NotFoundException("Cart not found with id: " + cartId);
        }
        if (!productService.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
    }

    private int getAvailableStock(Long productId) {
        Integer available = inventoryService.getAvailableStockQuantityByProductId(productId);
        return available == null ? 0 : available;
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
    @AuditAction(action = "CART_ITEMS_CLEARED", entityType = "CART_ITEM", idParamIndex = 0)
    public void deleteByCartId(Long cartId) {
        logger.info("Deleting cart items for cartId={}", cartId);
        List<CartItem> cartItems = findOrThrow(() -> getCartItemsByCartId(cartId),
                "No CartItems found for cartId: " + cartId);
        cartItems.forEach(this::deleteByCartItem);
    }

    @Override
    @Transactional
    @AuditAction(action = "CART_ITEM_DELETED", entityType = "CART_ITEM", idParamIndex = 0)
    public void deleteById(Long id) {
        deleteByCartItem(getCartItemById(id));
    }

    @Override
    @Transactional
    @AuditAction(action = "CART_ITEM_DELETED", entityType = "CART_ITEM")
    public void deleteByCartItem(CartItem cartItem) {
        Long productId = cartItem.getProduct().getId();
        int quantity = cartItem.getQuantity();
        Integer reserved = inventoryService.getTotalReservedQuantityByProductId(productId);
        int returnQty = reserved == null ? 0 : Math.min(quantity, reserved);
        if (returnQty > 0) {
            inventoryService.updateQuantityByCartItemAmountReturnedOrCheckouted(productId, returnQty, false);
        }
        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    @AuditAction(action = "CART_ITEM_BATCH_DELETED", entityType = "CART_ITEM")
    public void deleteByCartItems(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return;
        }
        Map<Long, Integer> quantityByProduct = cartItems.stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getId() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getId(),
                        Collectors.summingInt(CartItem::getQuantity)
                ));
        quantityByProduct.forEach((productId, quantity) -> {
            Integer reserved = inventoryService.getTotalReservedQuantityByProductId(productId);
            int returnQty = reserved == null ? 0 : Math.min(quantity, reserved);
            if (returnQty > 0) {
                inventoryService.updateQuantityByCartItemAmountReturnedOrCheckouted(productId, returnQty, true);
            }
        });
        cartItemRepository.deleteAllInBatch(cartItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByUpdatedAtDesc(cartId);
        if (cartItems.isEmpty()) {
            throw new NotFoundException("No CartItems found for cartId: " + cartId);
        }
        return cartItems;
    }

    @Override
    public List<CartItem> getCartItemsByActiveProductTrueAndCartId(Long cartId, ProductStatus status) {
        return cartItemRepository.findByActiveProductTrueAndCartId(cartId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CartItem> getCartItemsByCartId(Long cartId, Pageable pageable) {
        Page<CartItem> page = cartItemRepository.findByCartId(cartId, pageable);
        if (page.getTotalElements() == 0) {
            throw new NotFoundException("No CartItems found for cartId: " + cartId);
        }
        return page;
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
        List<CartItemResponse> responses = getUrlForCartService.toResponseListWithUrl(cartItemsPage.getContent());
        return new PageImpl<>(responses, cartItemsPage.getPageable(), cartItemsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItemResponsesByCartId(Long cartId, Long userId) {
        Cart cart = resolveCart(cartId, userId);
        List<CartItem> cartItems = getCartItemsByCartId(cart.getId());
        return getUrlForCartService.toResponseListWithUrl(cartItems);
    }

    @Override
    @Transactional(readOnly = true)
    public CartItemResponse getCartItemResponseByIdAndUserId(Long id, Long userId) {
        CartItem cartItem = (userId != null)
                ? getCartItemByIdAndUserId(id, userId)
                : getCartItemById(id);
        return getUrlForCartService.toResponseWithUrl(cartItem);
    }

    @Override
    @Transactional
    @AuditAction(action = "CART_ITEM_UPSERTED", entityType = "CART_ITEM")
    public CartItemResponse addOrUpdateCartItemResponse(CartItemRequest request, boolean isReturned, Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        return upsertCartItem(request, isReturned, cart.getId());
    }

    @Override
    @Transactional
    @AuditAction(action = "CART_ITEM_UPSERTED", entityType = "CART_ITEM")
    public CartItemResponse addOrUpdateCartItemResponseByCartId(CartItemRequest request, boolean isReturned, Long cartId) {
        return upsertCartItem(request, isReturned, cartId);
    }

    private CartItemResponse upsertCartItem(CartItemRequest request, boolean isReturned, Long cartId) {
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        validateCartAndProduct(cartId, productId);
        Cart cart = cartService.getCartById(cartId);
        if (!isReturned) {
            validateStock(productId, quantity);
        }

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .map(existingItem -> updateExistingItem(existingItem, quantity, isReturned, productId))
                .orElseGet(() -> createNewCartItem(request, cartId, productId, quantity));

        if (!isReturned) {
            Long userId = cart.getUser() != null ? cart.getUser().getId() : null;
            clickstreamEventService.recordAddToCart(userId, cart.getGuestId(), productId);
        }

        return getUrlForCartService.toResponseWithUrl(cartItem);
    }

    private CartItem updateExistingItem(CartItem existingItem, int quantity, boolean isReturned, Long productId) {
        if (isReturned) {
            cartItemRepository.updateQuantityById(quantity, existingItem.getId(), true);
            inventoryService.updateQuantityByCartItemAmountReturnedOrCheckouted(productId, quantity, false);
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
        return getUrlForCartService.toResponseListWithUrl(
                cartItemRepository.findByProductName(productName, userId)
        );
    }
}


