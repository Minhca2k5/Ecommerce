package com.minzetsu.ecommerce.common.config;

import com.minzetsu.ecommerce.user.entity.Role;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWhenUserExists() {
        Role role = Role.builder().name("ROLE_USER").build();

        User user = User.builder()
                .username("demo")
                .email("demo@test.com")
                .password("secret")
                .enabled(true)
                .roles(List.of(role))
                .build();
        user.setId(10L);

        when(userRepository.findByUsername("demo")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("demo");

        assertThat(details.getUsername()).isEqualTo("demo");
        assertThat(details.getPassword()).isEqualTo("secret");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(((CustomUserDetails) details).getId()).isEqualTo(10L);
    }

    @Test
    void loadUserByUsername_shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username: missing");
    }
}
