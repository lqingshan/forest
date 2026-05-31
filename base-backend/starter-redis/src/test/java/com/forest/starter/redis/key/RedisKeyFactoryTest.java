package com.forest.starter.redis.key;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisKeyFactoryTest {
    private final RedisKeyFactory redisKeys = new RedisKeyFactory("forest", "cxc-commerce", true, new RedisKeyValidator());

    @Test
    void createsRecommendedKeys() {
        assertEquals("forest:cxc-commerce:auth:sms:13800138000", redisKeys.authSms("13800138000").value());
        assertEquals("forest:cxc-commerce:auth:sms:send-limit:13800138000", redisKeys.authSmsSendLimit("13800138000").value());
        assertEquals("forest:cxc-commerce:auth:login-fail:phone:13800138000", redisKeys.authLoginFail("phone", "13800138000").value());
        assertEquals("forest:cxc-commerce:checkout:submit-token:ST202605090001", redisKeys.checkoutSubmitToken("ST202605090001").value());
        assertEquals("forest:cxc-commerce:payment:notify:wechat:420000001", redisKeys.paymentNotify("wechat", "420000001").value());
        assertEquals("forest:cxc-commerce:lock:order:create:10001", redisKeys.lock("order", "create", 10001L).value());
        assertEquals("forest:cxc-commerce:cache:product:spu:10001", redisKeys.cache("product", "spu", 10001L).value());
        assertEquals("forest:cxc-commerce:verification:sms-code:login:13800138000", redisKeys.verificationSmsCode("login", "13800138000").value());
        assertEquals("forest:cxc-commerce:verification:sms-attempt:login:13800138000", redisKeys.verificationSmsAttempt("login", "13800138000").value());
        assertEquals("forest:cxc-commerce:verification:sms-send-limit:login:13800138000", redisKeys.verificationSmsSendLimit("login", "13800138000").value());
        assertEquals("forest:cxc-commerce:verification:sms-daily-limit:login:13800138000:20260514", redisKeys.verificationSmsDailyLimit("login", "13800138000", "20260514").value());
        assertEquals("forest:cxc-commerce:verification:ticket:login:TICKET202605140001", redisKeys.verificationTicket("login", "TICKET202605140001").value());
    }

    @Test
    void rejectsInvalidStaticSegments() {
        assertThrows(IllegalArgumentException.class, () -> new RedisKeyFactory("forest", "", true, new RedisKeyValidator()));
        assertThrows(IllegalArgumentException.class, () -> new RedisKeyFactory("forest", "", false, new RedisKeyValidator()));
        assertThrows(IllegalArgumentException.class, () -> new RedisKeyFactory("forest", "CxcCommerce", true, new RedisKeyValidator()));
        assertThrows(IllegalArgumentException.class, () -> redisKeys.key("authDomain", "sms", "13800138000"));
        assertThrows(IllegalArgumentException.class, () -> redisKeys.key("auth", "sms_code", "13800138000"));
    }

    @Test
    void rejectsInvalidDynamicSegments() {
        assertThrows(IllegalArgumentException.class, () -> redisKeys.authSms(""));
        assertThrows(IllegalArgumentException.class, () -> redisKeys.authSms(null));
        assertThrows(IllegalArgumentException.class, () -> redisKeys.authSms("138:001"));
    }
}
