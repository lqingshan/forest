package com.forest.starter.redis.client;

import java.time.Duration;

import com.forest.starter.redis.key.RedisKey;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ForestRedisRateLimiterTest {
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final ForestRedisRateLimiter rateLimiter = new ForestRedisRateLimiter(redisTemplate);

    @Test
    void allowsFirstAcquireAndSetsWindowTtl() {
        RedisKey key = new RedisKey("forest:cxc-commerce:auth:sms:send-limit:13800138000");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key.value())).thenReturn(1L);

        ForestRedisRateLimiter.RateLimitResult result = rateLimiter.acquire(key, 1, Duration.ofMinutes(1));

        assertTrue(result.allowed());
        verify(redisTemplate).expire(key.value(), Duration.ofMinutes(1));
    }

    @Test
    void rejectsWhenFixedWindowCountExceedsMaxAttempts() {
        RedisKey key = new RedisKey("forest:cxc-commerce:auth:sms:send-limit:13800138000");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key.value())).thenReturn(2L);

        ForestRedisRateLimiter.RateLimitResult result = rateLimiter.acquire(key, 1, Duration.ofMinutes(1));

        assertFalse(result.allowed());
    }
}
