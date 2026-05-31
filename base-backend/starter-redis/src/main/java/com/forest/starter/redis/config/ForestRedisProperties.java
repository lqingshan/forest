package com.forest.starter.redis.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Forest Redis conventions.
 */
@ConfigurationProperties(prefix = "forest.redis")
public class ForestRedisProperties {
    private boolean enabled = true;
    private String keyPrefix = "forest";
    private String appCode;
    private boolean validateKey = true;
    private Ttl ttl = new Ttl();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public boolean isValidateKey() {
        return validateKey;
    }

    public void setValidateKey(boolean validateKey) {
        this.validateKey = validateKey;
    }

    public Ttl getTtl() {
        return ttl;
    }

    public void setTtl(Ttl ttl) {
        this.ttl = ttl;
    }

    public static class Ttl {
        private Duration smsCode = Duration.ofMinutes(5);
        private Duration loginFail = Duration.ofMinutes(15);
        private Duration submitToken = Duration.ofMinutes(30);
        private Duration checkoutSession = Duration.ofMinutes(30);
        private Duration idempotency = Duration.ofHours(24);
        private Duration paymentNotify = Duration.ofHours(24);
        private Duration lock = Duration.ofSeconds(30);
        private Duration cache = Duration.ofMinutes(30);

        public Duration getSmsCode() {
            return smsCode;
        }

        public void setSmsCode(Duration smsCode) {
            this.smsCode = smsCode;
        }

        public Duration getLoginFail() {
            return loginFail;
        }

        public void setLoginFail(Duration loginFail) {
            this.loginFail = loginFail;
        }

        public Duration getSubmitToken() {
            return submitToken;
        }

        public void setSubmitToken(Duration submitToken) {
            this.submitToken = submitToken;
        }

        public Duration getCheckoutSession() {
            return checkoutSession;
        }

        public void setCheckoutSession(Duration checkoutSession) {
            this.checkoutSession = checkoutSession;
        }

        public Duration getIdempotency() {
            return idempotency;
        }

        public void setIdempotency(Duration idempotency) {
            this.idempotency = idempotency;
        }

        public Duration getPaymentNotify() {
            return paymentNotify;
        }

        public void setPaymentNotify(Duration paymentNotify) {
            this.paymentNotify = paymentNotify;
        }

        public Duration getLock() {
            return lock;
        }

        public void setLock(Duration lock) {
            this.lock = lock;
        }

        public Duration getCache() {
            return cache;
        }

        public void setCache(Duration cache) {
            this.cache = cache;
        }
    }
}
