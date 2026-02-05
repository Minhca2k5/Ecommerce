package com.minzetsu.ecommerce.order.service;

import com.minzetsu.ecommerce.order.config.GuestCheckoutProperties;
import com.minzetsu.ecommerce.user.dto.request.UserCreateRequest;
import com.minzetsu.ecommerce.user.repository.UserRepository;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuestCheckoutIdentityService {

    private final GuestCheckoutProperties properties;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public Long resolveGuestCheckoutUserId() {
        return userRepository.findByUsername(properties.getUsername())
                .map(user -> user.getId())
                .orElseGet(() -> userService.createUserResponse(
                        UserCreateRequest.builder()
                                .username(properties.getUsername())
                                .email(properties.getEmail())
                                .password(properties.getPassword())
                                .enabled(true)
                                .build()
                ).getId());
    }
}
