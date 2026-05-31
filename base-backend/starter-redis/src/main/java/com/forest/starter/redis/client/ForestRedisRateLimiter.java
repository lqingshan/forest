package com.forest.starter.redis.client;

import java.time.Duration;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.redis.key.RedisKey;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Fixed-window Redis rate limiter.
 */
public class ForestRedisRateLimiter {
    private final StringRedisTemplate redisTemplate;

    public ForestRedisRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RateLimitResult acquire(RedisKey key, int maxAttempts, Duration window) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive.");
        }
        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive.");
        }

        Long current = redisTemplate.opsForValue().increment(key.value());
        if (current == null) {
            throw new BusinessException("Redis rate limit increment failed.");
        }
        if (current == 1L) {
            redisTemplate.expire(key.value(), window);
        }
        return new RateLimitResult(current <= maxAttempts, current, maxAttempts, window);
    }

    public record RateLimitResult(
        boolean allowed,
        long current,
        int maxAttempts,
        Duration window
    ) {
    }
}
