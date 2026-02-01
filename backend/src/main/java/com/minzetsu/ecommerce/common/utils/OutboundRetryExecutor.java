package com.minzetsu.ecommerce.common.utils;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.common.config.OutboundHttpProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.Callable;

@Component
@RequiredArgsConstructor
public class OutboundRetryExecutor {
    private final OutboundHttpProperties properties;

    

    public <T> T execute(Callable<T> call) {
        int attempts = 0;
        long backoff = properties.getBackoffMs();
        while (true) {
            try {
                return call.call();
            } catch (Exception ex) {
                attempts += 1;
                if (attempts >= properties.getMaxAttempts() || !isRetryable(ex)) {
                    if (ex instanceof RuntimeException runtime) {
                        throw runtime;
                    }
                    throw new RuntimeException("Outbound call failed", ex);
                }
                sleep(backoff);
                backoff = nextBackoff(backoff);
            }
        }
    }

    private boolean isRetryable(Exception ex) {
        if (ex instanceof ResourceAccessException) {
            return true;
        }
        if (ex instanceof HttpStatusCodeException httpEx) {
            return httpEx.getStatusCode().is5xxServerError();
        }
        return false;
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
