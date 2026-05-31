package com.forest.starter.json;

import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Factory helpers for protocol-specific JsonMapper copies.
 */
public final class ForestObjectMappers {
    private ForestObjectMappers() {
    }

    public static JsonMapper defaultJsonMapper() {
        return applyForestDefaults(JsonMapper.builder()).build();
    }

    public static JsonMapper copyForRedis(JsonMapper source) {
        return copyWithForestDefaults(source);
    }

    public static JsonMapper copyForHttpClient(JsonMapper source) {
        return copyWithForestDefaults(source);
    }

    private static JsonMapper copyWithForestDefaults(JsonMapper source) {
        JsonMapper.Builder builder = source == null ? JsonMapper.builder() : source.rebuild();
        return applyForestDefaults(builder)
            .deactivateDefaultTyping()
            .build();
    }

    private static JsonMapper.Builder applyForestDefaults(JsonMapper.Builder builder) {
        return builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
