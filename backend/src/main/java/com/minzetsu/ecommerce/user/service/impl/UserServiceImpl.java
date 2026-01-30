package com.minzetsu.ecommerce.user.service.impl;

import com.minzetsu.ecommerce.auth.repository.RefreshTokenRepository;
import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;
import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.cart.mapper.CartMapper;
import com.minzetsu.ecommerce.cart.repository.CartItemRepository;
import com.minzetsu.ecommerce.cart.repository.CartRepository;
import com.minzetsu.ecommerce.inventory.service.InventoryService;
import com.minzetsu.ecommerce.common.audit.AuditAction;
import com.minzetsu.ecommerce.common.exception.DeletionException;
import com.minzetsu.ecommerce.common.exception.InvalidCredentialException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.mapper.OrderMapper;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import com.minzetsu.ecommerce.review.mapper.ReviewMapper;
import com.minzetsu.ecommerce.review.repository.ReviewRepository;
import com.minzetsu.ecommerce.user.dto.filter.UserFilter;
import com.minzetsu.ecommerce.user.dto.request.PasswordRequest;
import com.minzetsu.ecommerce.user.dto.request.UserCreateRequest;
import com.minzetsu.ecommerce.user.dto.request.UserUpdateRequest;
import com.minzetsu.ecommerce.user.dto.response.AddressResponse;
import com.minzetsu.ecommerce.user.dto.response.UserResponse;
import com.minzetsu.ecommerce.user.entity.Role;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.mapper.AddressMapper;
import com.minzetsu.ecommerce.user.mapper.RoleMapper;
import com.minzetsu.ecommerce.user.mapper.UserMapper;
import com.minzetsu.ecommerce.user.repository.AddressRepository;
import com.minzetsu.ecommerce.user.repository.UserRepository;
import com.minzetsu.ecommerce.user.repository.UserSpecification;
import com.minzetsu.ecommerce.user.service.RoleService;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ReviewRepository reviewRepository;
    private final AddressRepository addressRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryService inventoryService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AddressMapper addressMapper;
    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;
    private final RoleMapper roleMapper;

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    @AuditAction(action = "USER_DELETED", entityType = "USER", idParamIndex = 0)
    public void deleteUser(Long id) {
        User user = getUserOrThrow(id);
        if (orderRepository.existsByUserId(id)) {
            throw new DeletionException("Cannot delete user with existing orders");
        }
        Cart cart = cartRepository.findByUserId(id).orElse(null);
        if (cart != null) {
            List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByUpdatedAtDesc(cart.getId());
            if (!cartItems.isEmpty()) {
                Map<Long, Integer> quantityByProduct = cartItems.stream()
                        .filter(item -> item.getProduct() != null && item.getProduct().getId() != null)
                        .collect(Collectors.groupingBy(
                                item -> item.getProduct().getId(),
                                Collectors.summingInt(CartItem::getQuantity)
                        ));
                quantityByProduct.forEach((productId, quantity) -> inventoryService.updateQuantityByCartItemAmountReturnedOrCheckouted(productId, quantity, false));
                cartItemRepository.deleteAllInBatch(cartItems);
            }
        }
        cartRepository.deleteByUserId(id);
        reviewRepository.deleteByUserId(id);
        addressRepository.deleteByUserId(id);
        refreshTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return getUserOrThrow(id);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserResponseById(Long id) {
        User user = getUserById(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getFullUserResponseById(Long id) {
        User user = getUserById(id);
        List<AddressResponse> addressResponses = addressMapper.toResponseList(addressRepository.findByUserIdOrderByUpdatedAtDesc(id));
        List<ReviewResponse> reviewResponses = reviewMapper.toResponseList(reviewRepository.findByUserIdOrderByUpdatedAtDesc(id));
        List<OrderResponse> orderResponses = orderMapper.toResponseList(orderRepository.findByUserIdOrderByUpdatedAtDesc(id));
        CartResponse cartResponse = cartMapper.toResponse(cartRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Cart not found for user id: " + id)));
        return userMapper.toFullResponse(user, addressResponses, reviewResponses, cartResponse, orderResponses, roleMapper);
    }

    @Override
    @Transactional
    @AuditAction(action = "USER_UPDATED", entityType = "USER", idParamIndex = 1)
    public UserResponse updateUserResponse(UserUpdateRequest request, Long id) {
        User user = getUserById(id);
        userMapper.updateUserFromRequest(request, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    @AuditAction(action = "USER_PASSWORD_CHANGED", entityType = "USER", idParamIndex = 0)
    public UserResponse changeUserPassword(Long id, PasswordRequest request) {
        User user = getUserById(id);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Old password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidCredentialException("New password must be different from the old password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    @AuditAction(action = "USER_CREATED", entityType = "USER")
    public UserResponse createUserResponse(UserCreateRequest request) {
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role defaultRole = roleService.getRoleByName("ROLE_USER");
        user.setRoles(List.of(defaultRole));
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUserResponses(UserFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                userRepository,
                UserSpecification.filter(filter),
                userMapper::toResponse
        );
    }
}
