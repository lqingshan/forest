package com.forest.starter.redis.client;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import com.forest.starter.redis.key.RedisKey;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * Lightweight Redis lock client based on SET NX PX and token-checked release.
 */
public class ForestRedisLockClient {
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
        Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public ForestRedisLockClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<RedisLockToken> tryLock(RedisKey key, Duration ttl) {
        return tryLock(key, UUID.randomUUID().toString(), ttl);
    }

    public Optional<RedisLockToken> tryLock(RedisKey key, String token, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive.");
        }
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key.value(), token, ttl);
        if (Boolean.TRUE.equals(locked)) {
            return Optional.of(new RedisLockToken(key, token, ttl));
        }
        return Optional.empty();
    }

    public boolean release(RedisLockToken lockToken) {
        return release(lockToken.key(), lockToken.token());
    }

    public boolean release(RedisKey key, String token) {
        Long result = redisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(key.value()), token);
        return Long.valueOf(1L).equals(result);
    }

    public record RedisLockToken(
        RedisKey key,
        String token,
        Duration ttl
    ) {
    }
}
