package com.minzetsu.ecommerce.order.service.impl;

import com.minzetsu.ecommerce.common.exception.AppException;
import com.minzetsu.ecommerce.order.config.CheckoutAbuseProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutAbuseServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CheckoutAbuseProperties properties;
    private CheckoutAbuseServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new CheckoutAbuseProperties();
        properties.setEnabled(true);
        properties.setMaxFailures(3);
        properties.setWindowMinutes(10);

        service = new CheckoutAbuseServiceImpl(properties, redisTemplate);
    }

    @Test
    void assertAllowed_shouldThrowWhenFailureCountReachedLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("checkout:abuse:ip-1")).thenReturn("3");

        assertThatThrownBy(() -> service.assertAllowed("ip-1"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Too many failed checkout attempts");
    }

    @Test
    void assertAllowed_shouldPassWhenFailureCountBelowLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("checkout:abuse:ip-2")).thenReturn("2");

        assertThatCode(() -> service.assertAllowed("ip-2"))
                .doesNotThrowAnyException();
    }

    @Test
    void recordFailure_shouldIncrementAndSetExpireOnFirstFailure() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("checkout:abuse:ip-3")).thenReturn(1L);

        service.recordFailure("ip-3");

        verify(valueOperations).increment("checkout:abuse:ip-3");
        verify(redisTemplate).expire("checkout:abuse:ip-3", Duration.ofMinutes(10));
    }

    @Test
    void recordSuccess_shouldDeleteCounterKey() {
        service.recordSuccess("ip-4");

        verify(redisTemplate).delete("checkout:abuse:ip-4");
    }

    @Test
    void shouldBypassAllActionsWhenGuardDisabled() {
        properties.setEnabled(false);

        assertThatCode(() -> service.assertAllowed("ip-5")).doesNotThrowAnyException();
        service.recordFailure("ip-5");
        service.recordSuccess("ip-5");

        verify(redisTemplate, never()).opsForValue();
        verify(redisTemplate, never()).delete("checkout:abuse:ip-5");
    }
}
