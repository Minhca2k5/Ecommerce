package com.minzetsu.ecommerce.common.utils;

import com.minzetsu.ecommerce.common.config.OutboundHttpProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
public class OutboundRetryExecutor {
    private final OutboundHttpProperties properties;

    public OutboundRetryExecutor(OutboundHttpProperties properties) {
        this.properties = properties;
    }

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
        return true;
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
