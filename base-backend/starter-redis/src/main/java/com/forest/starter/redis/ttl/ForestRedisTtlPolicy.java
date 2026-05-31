package com.forest.starter.redis.ttl;

import java.time.Duration;

import com.forest.starter.redis.config.ForestRedisProperties;

/**
 * Provides named Redis TTL values.
 */
public class ForestRedisTtlPolicy {
    private final ForestRedisProperties.Ttl ttl;

    public ForestRedisTtlPolicy(ForestRedisProperties properties) {
        this.ttl = properties.getTtl();
    }

    public Duration smsCode() {
        return ttl.getSmsCode();
    }

    public Duration loginFail() {
        return ttl.getLoginFail();
    }

    public Duration submitToken() {
        return ttl.getSubmitToken();
    }

    public Duration checkoutSession() {
        return ttl.getCheckoutSession();
    }

    public Duration idempotency() {
        return ttl.getIdempotency();
    }

    public Duration paymentNotify() {
        return ttl.getPaymentNotify();
    }

    public Duration lock() {
        return ttl.getLock();
    }

    public Duration cache() {
        return ttl.getCache();
    }
}
