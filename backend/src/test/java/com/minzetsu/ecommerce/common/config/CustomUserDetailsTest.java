package com.minzetsu.ecommerce.common.config;

import com.minzetsu.ecommerce.user.entity.Role;
import com.minzetsu.ecommerce.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void shouldExposeUserFieldsAndAuthorities() {
        Role userRole = Role.builder().name("ROLE_USER").build();
        Role adminRole = Role.builder().name("ROLE_ADMIN").build();

        User user = User.builder()
                .username("alice")
                .email("alice@test.com")
                .password("secret")
                .enabled(true)
                .roles(List.of(userRole, adminRole))
                .build();
        user.setId(42L);

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.getId()).isEqualTo(42L);
        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("secret");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_USER", "ROLE_ADMIN");
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }
}
