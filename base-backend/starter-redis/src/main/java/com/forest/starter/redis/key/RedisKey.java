package com.forest.starter.redis.key;

/**
 * Represents a validated Redis key.
 */
public record RedisKey(String value) {
    public RedisKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Redis key must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
