package com.minzetsu.ecommerce.common.utils;

import com.minzetsu.ecommerce.common.config.OutboundHttpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundRetryExecutorTest {

    private OutboundRetryExecutor executor;

    @BeforeEach
    void setUp() {
        OutboundHttpProperties properties = new OutboundHttpProperties();
        properties.setMaxAttempts(3);
        properties.setBackoffMs(0);
        properties.setBackoffMultiplier(2.0);
        properties.setMaxBackoffMs(1);
        executor = new OutboundRetryExecutor(properties);
    }

    @Test
    void execute_shouldRetryOnResourceAccessExceptionAndEventuallySucceed() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = executor.execute(() -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw new ResourceAccessException("timeout");
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void execute_shouldThrowAfterMaxAttemptsForRetryableException() {
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> executor.execute(() -> {
            attempts.incrementAndGet();
            throw new ResourceAccessException("timeout");
        }))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("timeout");

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void execute_shouldThrowOriginalRuntimeExceptionWhenNonRetryable() {
        IllegalArgumentException ex = new IllegalArgumentException("bad request");

        assertThatThrownBy(() -> executor.execute(() -> {
            throw ex;
        }))
                .isSameAs(ex);
    }

    @Test
    void execute_shouldWrapCheckedExceptionWhenNonRetryable() {
        assertThatThrownBy(() -> executor.execute(() -> {
            throw new Exception("io fail");
        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Outbound call failed")
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void execute_shouldRetryOnHttp5xxAndEventuallySucceed() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = executor.execute(() -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw HttpServerErrorException.create(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Service Unavailable",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        null
                );
            }
            return "ok-5xx";
        });

        assertThat(result).isEqualTo("ok-5xx");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void execute_shouldNotRetryOnHttp4xx() {
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> executor.execute(() -> {
            attempts.incrementAndGet();
            throw HttpClientErrorException.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    HttpHeaders.EMPTY,
                    new byte[0],
                    null
            );
        })).isInstanceOf(HttpClientErrorException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }
}
