package com.forest.verification.service;

import java.time.Duration;

/**
 * Result of sending an SMS verification code.
 */
public record SendSmsCodeResult(
    String phone,
    Duration ttl
) {
    public int ttlMinutes() {
        return (int) Math.max(1, ttl.toMinutes());
    }
}
