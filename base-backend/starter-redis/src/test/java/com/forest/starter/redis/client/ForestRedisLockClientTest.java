package com.forest.starter.redis.client;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

import com.forest.starter.redis.client.ForestRedisLockClient.RedisLockToken;
import com.forest.starter.redis.key.RedisKey;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class ForestRedisLockClientTest {
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final ForestRedisLockClient lockClient = new ForestRedisLockClient(redisTemplate);

    @Test
    void returnsTokenWhenLockIsAcquired() {
        RedisKey key = new RedisKey("forest:cxc-commerce:lock:order:create:10001");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(key.value(), "token-1", Duration.ofSeconds(30))).thenReturn(true);

        Optional<RedisLockToken> lockToken = lockClient.tryLock(key, "token-1", Duration.ofSeconds(30));

        assertTrue(lockToken.isPresent());
        assertEquals("token-1", lockToken.get().token());
    }

    @Test
    void returnsEmptyWhenLockIsOccupied() {
        RedisKey key = new RedisKey("forest:cxc-commerce:lock:order:create:10001");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(key.value(), "token-1", Duration.ofSeconds(30))).thenReturn(false);

        Optional<RedisLockToken> lockToken = lockClient.tryLock(key, "token-1", Duration.ofSeconds(30));

        assertTrue(lockToken.isEmpty());
    }

    @Test
    void releaseRequiresMatchingTokenScriptResult() {
        RedisKey key = new RedisKey("forest:cxc-commerce:lock:order:create:10001");
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(key.value())), eq("token-1"))).thenReturn(1L);
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(key.value())), eq("token-2"))).thenReturn(0L);

        assertTrue(lockClient.release(key, "token-1"));
        assertFalse(lockClient.release(key, "token-2"));
    }
}
