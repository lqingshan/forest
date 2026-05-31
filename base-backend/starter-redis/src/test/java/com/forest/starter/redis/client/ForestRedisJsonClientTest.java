package com.forest.starter.redis.client;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.forest.starter.redis.key.RedisKey;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ForestRedisJsonClientTest {
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final ForestRedisJsonClient redisJsonClient = new ForestRedisJsonClient(redisTemplate, JsonMapper.builder().build());

    @Test
    void storesObjectAsPlainJsonString() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisJsonClient.set(new RedisKey("forest:cxc-commerce:auth:session:1"), new SessionSnapshot(1L, "wechat-miniapp"), Duration.ofMinutes(5));

        verify(valueOperations).set("forest:cxc-commerce:auth:session:1", "{\"userId\":1,\"clientType\":\"wechat-miniapp\"}", Duration.ofMinutes(5));
    }

    @Test
    void storesJavaTimeObjectAsPlainJsonString() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisJsonClient.set(
            new RedisKey("forest:cxc-commerce:verification:sms-code:login:13800138000"),
            new CodeSnapshot(LocalDateTime.of(2026, 5, 21, 20, 4, 18)),
            Duration.ofMinutes(5)
        );

        verify(valueOperations).set(
            "forest:cxc-commerce:verification:sms-code:login:13800138000",
            "{\"issuedAt\":\"2026-05-21T20:04:18\"}",
            Duration.ofMinutes(5)
        );
    }

    @Test
    void readsObjectByClass() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("forest:cxc-commerce:auth:session:1")).thenReturn("{\"userId\":1,\"clientType\":\"wechat-miniapp\"}");

        Optional<SessionSnapshot> snapshot = redisJsonClient.get(new RedisKey("forest:cxc-commerce:auth:session:1"), SessionSnapshot.class);

        assertTrue(snapshot.isPresent());
        assertEquals(1L, snapshot.get().userId());
        assertEquals("wechat-miniapp", snapshot.get().clientType());
    }

    @Test
    void readsObjectByTypeReference() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("forest:cxc-commerce:cache:test:list")).thenReturn("[{\"userId\":1,\"clientType\":\"wechat-miniapp\"}]");

        Optional<List<SessionSnapshot>> snapshots = redisJsonClient.get(
            new RedisKey("forest:cxc-commerce:cache:test:list"),
            new TypeReference<>() {
            }
        );

        assertTrue(snapshots.isPresent());
        assertEquals(1, snapshots.get().size());
        assertEquals(1L, snapshots.get().getFirst().userId());
    }

    record SessionSnapshot(Long userId, String clientType) {
    }

    record CodeSnapshot(LocalDateTime issuedAt) {
    }
}
