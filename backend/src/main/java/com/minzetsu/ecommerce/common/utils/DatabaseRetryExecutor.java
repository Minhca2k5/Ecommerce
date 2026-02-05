package com.minzetsu.ecommerce.common.utils;

import com.minzetsu.ecommerce.common.config.DbRetryProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.concurrent.Callable;

@Component
@RequiredArgsConstructor
public class DatabaseRetryExecutor {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseRetryExecutor.class);

    private final DbRetryProperties properties;

    public <T> T execute(String operation, Callable<T> call) {
        int attempts = 0;
        long backoff = properties.getBackoffMs();

        while (true) {
            try {
                return call.call();
            } catch (Exception ex) {
                attempts += 1;
                boolean retryable = isRetryable(ex);
                if (!retryable || attempts >= properties.getMaxAttempts()) {
                    if (ex instanceof RuntimeException runtime) {
                        throw runtime;
                    }
                    throw new RuntimeException("Database operation failed: " + operation, ex);
                }
                logger.warn(
                        "Retrying DB operation '{}' after transient lock failure (attempt {}/{}): {}",
                        operation,
                        attempts,
                        properties.getMaxAttempts(),
                        ex.getMessage()
                );
                sleep(backoff);
                backoff = nextBackoff(backoff);
            }
        }
    }

    private boolean isRetryable(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof DeadlockLoserDataAccessException
                    || current instanceof CannotAcquireLockException
                    || current instanceof PessimisticLockingFailureException
                    || current instanceof CannotSerializeTransactionException) {
                return true;
            }
            if (current instanceof SQLException sqlEx && isRetryableSql(sqlEx)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isRetryableSql(SQLException ex) {
        String state = ex.getSQLState();
        int code = ex.getErrorCode();
        return "40001".equals(state) || code == 1213 || code == 1205;
    }

    private long nextBackoff(long current) {
        long next = (long) (current * properties.getBackoffMultiplier());
        return Math.min(next, properties.getMaxBackoffMs());
    }

    private void sleep(long backoffMs) {
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
