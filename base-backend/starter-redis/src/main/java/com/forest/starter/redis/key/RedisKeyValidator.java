package com.forest.starter.redis.key;

import java.util.regex.Pattern;

/**
 * Validates Forest Redis key segments.
 */
public class RedisKeyValidator {
    private static final Pattern KEBAB_CASE = Pattern.compile("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$");

    public void validateStaticSegment(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        if (!KEBAB_CASE.matcher(value).matches()) {
            throw new IllegalArgumentException(name + " must be lower kebab-case: " + value);
        }
    }

    public void validateDynamicSegment(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null.");
        }
        String text = String.valueOf(value);
        if (text.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        if (text.contains(":")) {
            throw new IllegalArgumentException(name + " must not contain ':'.");
        }
    }
}
