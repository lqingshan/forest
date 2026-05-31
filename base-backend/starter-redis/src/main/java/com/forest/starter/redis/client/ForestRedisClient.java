package com.forest.starter.redis.client;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import com.forest.starter.redis.key.RedisKey;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * String-oriented Redis operations for Forest keys.
 */
public class ForestRedisClient {
    private final StringRedisTemplate redisTemplate;

    public ForestRedisClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<String> get(RedisKey key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key.value()));
    }

    public Optional<String> getAndDelete(RedisKey key) {
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(key.value()));
    }

    public void set(RedisKey key, String value, Duration ttl) {
        requireTtl(ttl);
        redisTemplate.opsForValue().set(key.value(), Objects.requireNonNull(value, "value must not be null"), ttl);
    }

    public void set(RedisKey key, String value) {
        redisTemplate.opsForValue().set(key.value(), Objects.requireNonNull(value, "value must not be null"));
    }

    public boolean setIfAbsent(RedisKey key, String value, Duration ttl) {
        requireTtl(ttl);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key.value(), Objects.requireNonNull(value, "value must not be null"), ttl);
        return Boolean.TRUE.equals(success);
    }

    public long increment(RedisKey key) {
        Long value = redisTemplate.opsForValue().increment(key.value());
        return value == null ? 0L : value;
    }

    public boolean expire(RedisKey key, Duration ttl) {
        requireTtl(ttl);
        return Boolean.TRUE.equals(redisTemplate.expire(key.value(), ttl));
    }

    public boolean delete(RedisKey key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key.value()));
    }

    private void requireTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive.");
        }
    }
}
