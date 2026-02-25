package com.minzetsu.ecommerce.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
public class RedisRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitService.class);

    private final StringRedisTemplate redisTemplate;

    // Lua script: atomic token-bucket check-and-consume
    // KEYS[1] = bucket key
    // ARGV[1] = capacity, ARGV[2] = refillTokens, ARGV[3] = refillPeriodMs, ARGV[4]
    // = ttlSeconds
    // Returns 1 if allowed, 0 if rate-limited
    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refillTokens = tonumber(ARGV[2])
            local refillPeriodMs = tonumber(ARGV[3])
            local ttlSeconds = tonumber(ARGV[4])
            local now = tonumber(redis.call('TIME')[1]) * 1000 + tonumber(redis.call('TIME')[2]) / 1000

            local bucket = redis.call('HMGET', key, 'tokens', 'lastRefill')
            local tokens = tonumber(bucket[1])
            local lastRefill = tonumber(bucket[2])

            if tokens == nil then
                tokens = capacity
                lastRefill = now
            end

            local elapsed = now - lastRefill
            if elapsed >= refillPeriodMs then
                local periods = math.floor(elapsed / refillPeriodMs)
                tokens = math.min(capacity, tokens + periods * refillTokens)
                lastRefill = lastRefill + periods * refillPeriodMs
            end

            local allowed = 0
            if tokens > 0 then
                tokens = tokens - 1
                allowed = 1
            end

            redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', lastRefill)
            redis.call('EXPIRE', key, ttlSeconds)

            return allowed
            """;

    private final DefaultRedisScript<Long> rateLimitScript;

    public RedisRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
    }

    /**
     * Try to consume one token from the bucket identified by {@code bucketKey}.
     *
     * @return true if the request is allowed, false if rate-limited.
     *         On Redis failure, returns true (fail-open) so requests are not
     *         blocked.
     */
    public boolean tryConsume(String bucketKey, int capacity, int refillTokens, Duration period, Duration bucketTtl) {
        try {
            Long result = redisTemplate.execute(
                    rateLimitScript,
                    Collections.singletonList("rate_limit:" + bucketKey),
                    String.valueOf(capacity),
                    String.valueOf(refillTokens),
                    String.valueOf(period.toMillis()),
                    String.valueOf(bucketTtl.getSeconds()));
            return result != null && result == 1L;
        } catch (Exception ex) {
            log.warn("Redis rate-limit unavailable, allowing request (fail-open): {}", ex.getMessage());
            return true;
        }
    }
}
