package com.forest.starter.json;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestObjectMappersTest {
    @Test
    void redisCopySupportsJavaTimeWithoutMutatingSourceMapper() {
        JsonMapper source = JsonMapper.builder().build();

        JsonMapper redisMapper = ForestObjectMappers.copyForRedis(source);

        assertNotSame(source, redisMapper);
        assertEquals(
            "{\"issuedAt\":\"2026-05-22T10:30:45\"}",
            redisMapper.writeValueAsString(new TimePayload(LocalDateTime.of(2026, 5, 22, 10, 30, 45)))
        );
    }

    @Test
    void redisCopyRemovesDefaultTypingClassMetadata() {
        JsonMapper source = JsonMapper.builder()
            .activateDefaultTypingAsProperty(
                BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(),
                DefaultTyping.NON_FINAL_AND_RECORDS,
                "@class"
            )
            .build();
        TimePayload payload = new TimePayload(LocalDateTime.of(2026, 5, 22, 10, 30, 45));

        assertTrue(source.writeValueAsString(payload).contains("\"@class\""));

        String json = ForestObjectMappers.copyForRedis(source).writeValueAsString(payload);

        assertFalse(json.contains("\"@class\""));
        assertEquals("{\"issuedAt\":\"2026-05-22T10:30:45\"}", json);
    }

    record TimePayload(LocalDateTime issuedAt) {
    }
}
