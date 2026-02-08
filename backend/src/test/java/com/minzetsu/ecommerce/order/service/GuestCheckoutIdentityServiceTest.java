package com.minzetsu.ecommerce.order.service;

import com.minzetsu.ecommerce.order.config.GuestCheckoutProperties;
import com.minzetsu.ecommerce.user.dto.request.UserCreateRequest;
import com.minzetsu.ecommerce.user.dto.response.UserResponse;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.repository.UserRepository;
import com.minzetsu.ecommerce.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestCheckoutIdentityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    private GuestCheckoutProperties properties;
    private GuestCheckoutIdentityService identityService;

    @BeforeEach
    void setUp() {
        properties = new GuestCheckoutProperties();
        properties.setUsername("guest_checkout");
        properties.setEmail("guest.checkout@local.invalid");
        properties.setPassword("GuestCheckout@123");

        identityService = new GuestCheckoutIdentityService(properties, userRepository, userService);
    }

    @Test
    void resolveGuestCheckoutUserId_shouldReturnExistingUserId() {
        User existing = User.builder()
                .username("guest_checkout")
                .email("guest.checkout@local.invalid")
                .password("pw")
                .build();
        existing.setId(777L);

        when(userRepository.findByUsername("guest_checkout")).thenReturn(Optional.of(existing));

        Long userId = identityService.resolveGuestCheckoutUserId();

        assertThat(userId).isEqualTo(777L);
        verify(userService, never()).createUserResponse(org.mockito.ArgumentMatchers.any(UserCreateRequest.class));
    }

    @Test
    void resolveGuestCheckoutUserId_shouldCreateUserWhenMissing() {
        when(userRepository.findByUsername("guest_checkout")).thenReturn(Optional.empty());

        UserResponse created = UserResponse.builder().username("guest_checkout").build();
        created.setId(888L);
        when(userService.createUserResponse(org.mockito.ArgumentMatchers.any(UserCreateRequest.class))).thenReturn(created);

        Long userId = identityService.resolveGuestCheckoutUserId();

        assertThat(userId).isEqualTo(888L);

        ArgumentCaptor<UserCreateRequest> requestCaptor = ArgumentCaptor.forClass(UserCreateRequest.class);
        verify(userService).createUserResponse(requestCaptor.capture());

        UserCreateRequest request = requestCaptor.getValue();
        assertThat(request.getUsername()).isEqualTo("guest_checkout");
        assertThat(request.getEmail()).isEqualTo("guest.checkout@local.invalid");
        assertThat(request.getPassword()).isEqualTo("GuestCheckout@123");
        assertThat(request.getEnabled()).isTrue();
    }
}
