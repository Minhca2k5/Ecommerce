package com.minzetsu.ecommerce.common.utils;

import com.minzetsu.ecommerce.common.config.DbRetryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DeadlockLoserDataAccessException;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseRetryExecutorTest {

    private DatabaseRetryExecutor executor;

    @BeforeEach
    void setUp() {
        DbRetryProperties properties = new DbRetryProperties();
        properties.setMaxAttempts(3);
        properties.setBackoffMs(0);
        properties.setBackoffMultiplier(2.0);
        properties.setMaxBackoffMs(1);

        executor = new DatabaseRetryExecutor(properties);
    }

    @Test
    void execute_shouldRetryTransientDeadlockAndSucceed() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = executor.execute("order-create", () -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw new DeadlockLoserDataAccessException("deadlock", new RuntimeException("mysql-1213"));
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void execute_shouldRetryWhenCauseContainsRetryableSqlState() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = executor.execute("order-update", () -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw new RuntimeException(new SQLException("serialization failure", "40001", 1213));
            }
            return "done";
        });

        assertThat(result).isEqualTo("done");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void execute_shouldThrowAfterMaxAttemptsForRetryableError() {
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> executor.execute("order-create", () -> {
            attempts.incrementAndGet();
            throw new DeadlockLoserDataAccessException("deadlock", new RuntimeException("mysql-1213"));
        }))
                .isInstanceOf(DeadlockLoserDataAccessException.class)
                .hasMessageContaining("deadlock");

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void execute_shouldThrowImmediatelyForNonRetryableRuntimeException() {
        IllegalStateException ex = new IllegalStateException("boom");

        assertThatThrownBy(() -> executor.execute("order-create", () -> {
            throw ex;
        }))
                .isSameAs(ex);
    }

    @Test
    void execute_shouldWrapCheckedRetryableExceptionAfterMaxAttempts() {
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> executor.execute("order-sync", () -> {
            attempts.incrementAndGet();
            throw new SQLException("lock wait timeout", "40001", 1205);
        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database operation failed: order-sync")
                .hasCauseInstanceOf(SQLException.class);

        assertThat(attempts.get()).isEqualTo(3);
    }
}
