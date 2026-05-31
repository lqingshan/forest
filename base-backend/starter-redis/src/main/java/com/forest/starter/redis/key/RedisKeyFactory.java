package com.forest.starter.redis.key;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates Redis keys that follow forest:{app}:{domain}:{resource}:{id...}.
 */
public class RedisKeyFactory {
    private final String keyPrefix;
    private final String appCode;
    private final boolean validateKey;
    private final RedisKeyValidator validator;

    public RedisKeyFactory(String keyPrefix, String appCode, boolean validateKey, RedisKeyValidator validator) {
        this.keyPrefix = keyPrefix;
        this.appCode = appCode;
        this.validateKey = validateKey;
        this.validator = validator;
        requireText("forest.redis.key-prefix", keyPrefix);
        requireText("forest.redis.app-code", appCode);
        if (validateKey) {
            validator.validateStaticSegment("forest.redis.key-prefix", keyPrefix);
            validator.validateStaticSegment("forest.redis.app-code", appCode);
        }
    }

    public RedisKey authSms(Object phone) {
        return key("auth", "sms", phone);
    }

    public String appCode() {
        return appCode;
    }

    public RedisKey authSmsSendLimit(Object phone) {
        return key("auth", "sms", "send-limit", phone);
    }

    public RedisKey authLoginFail(String accountType, Object identifier) {
        return key("auth", "login-fail", accountType, identifier);
    }

    public RedisKey authSession(Object sessionId) {
        return key("auth", "session", sessionId);
    }

    public RedisKey authRefreshJti(Object jti) {
        return key("auth", "refresh-jti", jti);
    }

    public RedisKey verificationSmsCode(String scene, Object phone) {
        return key("verification", "sms-code", scene, phone);
    }

    public RedisKey verificationSmsAttempt(String scene, Object phone) {
        return key("verification", "sms-attempt", scene, phone);
    }

    public RedisKey verificationSmsSendLimit(String scene, Object phone) {
        return key("verification", "sms-send-limit", scene, phone);
    }

    public RedisKey verificationSmsDailyLimit(String scene, Object phone, Object date) {
        return key("verification", "sms-daily-limit", scene, phone, date);
    }

    public RedisKey verificationTicket(String scene, Object ticketNo) {
        return key("verification", "ticket", scene, ticketNo);
    }

    public RedisKey checkoutSession(Object checkoutSessionId) {
        return key("checkout", "session", checkoutSessionId);
    }

    public RedisKey checkoutSubmitToken(Object submitToken) {
        return key("checkout", "submit-token", submitToken);
    }

    public RedisKey checkoutFingerprint(Object requestFingerprint) {
        return key("checkout", "fingerprint", requestFingerprint);
    }

    public RedisKey paymentNotify(String channel, Object notifyId) {
        return key("payment", "notify", channel, notifyId);
    }

    public RedisKey lock(String domain, String action, Object... ids) {
        return key("lock", domain, prepend(action, ids));
    }

    public RedisKey cache(String domain, String resource, Object... ids) {
        return key("cache", domain, prepend(resource, ids));
    }

    public RedisKey key(String domain, String resource, Object... segments) {
        if (validateKey) {
            validator.validateStaticSegment("domain", domain);
            validator.validateStaticSegment("resource", resource);
        }

        List<String> parts = new ArrayList<>();
        parts.add(keyPrefix);
        parts.add(appCode);
        parts.add(domain);
        parts.add(resource);
        for (int i = 0; i < segments.length; i++) {
            Object segment = segments[i];
            if (validateKey) {
                validator.validateDynamicSegment("segment[" + i + "]", segment);
            }
            parts.add(String.valueOf(segment));
        }
        return new RedisKey(String.join(":", parts));
    }

    private Object[] prepend(Object first, Object[] rest) {
        Object[] values = new Object[rest.length + 1];
        values[0] = first;
        System.arraycopy(rest, 0, values, 1, rest.length);
        return values;
    }

    private void requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
    }
}
