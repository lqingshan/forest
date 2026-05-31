package com.forest.starter.redis.client;

import java.time.Duration;
import java.util.Optional;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.json.ForestObjectMappers;
import com.forest.starter.redis.key.RedisKey;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSON Redis operations backed by Jackson and StringRedisTemplate.
 */
public class ForestRedisJsonClient {
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper objectMapper;

    public ForestRedisJsonClient(StringRedisTemplate redisTemplate, JsonMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = ForestObjectMappers.copyForRedis(objectMapper);
    }

    public <T> void set(RedisKey key, T value, Duration ttl) {
        requireTtl(ttl);
        try {
            redisTemplate.opsForValue().set(key.value(), objectMapper.writeValueAsString(value), ttl);
        } catch (JacksonException ex) {
            throw new BusinessException("Redis JSON serialization failed.", ex);
        }
    }

    public <T> Optional<T> get(RedisKey key, Class<T> type) {
        String json = redisTemplate.opsForValue().get(key.value());
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (JacksonException ex) {
            throw new BusinessException("Redis JSON deserialization failed.", ex);
        }
    }

    public <T> Optional<T> getAndDelete(RedisKey key, Class<T> type) {
        String json = redisTemplate.opsForValue().getAndDelete(key.value());
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (JacksonException ex) {
            throw new BusinessException("Redis JSON deserialization failed.", ex);
        }
    }

    public <T> Optional<T> get(RedisKey key, TypeReference<T> typeReference) {
        String json = redisTemplate.opsForValue().get(key.value());
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, typeReference));
        } catch (JacksonException ex) {
            throw new BusinessException("Redis JSON deserialization failed.", ex);
        }
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
