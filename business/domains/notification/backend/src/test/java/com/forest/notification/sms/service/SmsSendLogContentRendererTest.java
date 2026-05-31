package com.forest.notification.sms.service;

import java.util.Map;

import com.forest.starter.sms.config.ForestSmsProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SmsSendLogContentRendererTest {
    private final SmsSendLogContentRenderer renderer = new SmsSendLogContentRenderer();

    @Test
    void rendersPartialMaskedCodeByDefault() {
        ForestSmsProperties.Log log = new ForestSmsProperties.Log();

        String content = renderer.render("您的验证码为 {code}", Map.of("code", "123456"), log);

        assertEquals("您的验证码为 ****56", content);
    }

    @Test
    void rendersFullyMaskedCode() {
        ForestSmsProperties.Log log = new ForestSmsProperties.Log();
        log.setContentMode(ForestSmsProperties.ContentMode.MASKED);

        String content = renderer.render("您的验证码为 {code}", Map.of("code", "123456"), log);

        assertEquals("您的验证码为 ******", content);
    }

    @Test
    void rendersPlainContentWhenConfigured() {
        ForestSmsProperties.Log log = new ForestSmsProperties.Log();
        log.setContentMode(ForestSmsProperties.ContentMode.PLAIN);

        String content = renderer.render("您的验证码为 {code}", Map.of("code", "123456"), log);

        assertEquals("您的验证码为 123456", content);
    }

    @Test
    void returnsNullWhenContentSnapshotIsDisabled() {
        ForestSmsProperties.Log log = new ForestSmsProperties.Log();
        log.setContentMode(ForestSmsProperties.ContentMode.NONE);

        String content = renderer.render("您的验证码为 {code}", Map.of("code", "123456"), log);

        assertNull(content);
    }
}
