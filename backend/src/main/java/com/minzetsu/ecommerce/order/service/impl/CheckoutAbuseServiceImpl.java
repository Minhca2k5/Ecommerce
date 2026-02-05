package com.minzetsu.ecommerce.order.service.impl;

import com.minzetsu.ecommerce.common.exception.AppException;
import com.minzetsu.ecommerce.order.config.CheckoutAbuseProperties;
import com.minzetsu.ecommerce.order.service.CheckoutAbuseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutAbuseServiceImpl implements CheckoutAbuseService {
    private static final String PREFIX = "checkout:abuse:";

    private final CheckoutAbuseProperties properties;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void assertAllowed(String scope) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            String value = redisTemplate.opsForValue().get(key(scope));
            int failures = value == null ? 0 : Integer.parseInt(value);
            if (failures >= properties.getMaxFailures()) {
                throw new AppException(
                        "Too many failed checkout attempts. Please wait and try again.",
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Checkout abuse guard read failed, bypassing guard: {}", ex.getMessage());
        }
    }

    @Override
    public void recordFailure(String scope) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            String key = key(scope);
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofMinutes(properties.getWindowMinutes()));
            }
        } catch (Exception ex) {
            log.warn("Checkout abuse guard write failed: {}", ex.getMessage());
        }
    }

    @Override
    public void recordSuccess(String scope) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            redisTemplate.delete(key(scope));
        } catch (Exception ex) {
            log.warn("Checkout abuse guard reset failed: {}", ex.getMessage());
        }
    }

    private String key(String scope) {
        return PREFIX + scope;
    }
}
