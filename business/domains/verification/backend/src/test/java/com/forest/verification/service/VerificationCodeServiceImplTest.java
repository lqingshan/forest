package com.forest.verification.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Optional;

import com.forest.notification.sms.entity.SmsSendStatus;
import com.forest.notification.sms.service.SmsSendCommand;
import com.forest.notification.sms.service.SmsSendService;
import com.forest.starter.redis.client.ForestRedisClient;
import com.forest.starter.redis.client.ForestRedisJsonClient;
import com.forest.starter.redis.key.RedisKey;
import com.forest.starter.redis.key.RedisKeyFactory;
import com.forest.starter.redis.key.RedisKeyValidator;
import com.forest.starter.sms.config.ForestSmsProperties;
import com.forest.verification.config.ForestVerificationProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationCodeServiceImplTest {
    private final RedisKeyFactory redisKeys = new RedisKeyFactory("forest", "cxc-commerce", true, new RedisKeyValidator());
    private final ForestRedisClient redisClient = mock(ForestRedisClient.class);
    private final ForestRedisJsonClient redisJsonClient = mock(ForestRedisJsonClient.class);
    private final SmsSendService smsSendService = mock(SmsSendService.class);
    private final ForestVerificationProperties verificationProperties = new ForestVerificationProperties();
    private final ForestSmsProperties smsProperties = new ForestSmsProperties();
    private final VerificationCodeServiceImpl service = new VerificationCodeServiceImpl(
        redisKeys,
        redisClient,
        redisJsonClient,
        smsSendService,
        verificationProperties,
        smsProperties
    );

    @Test
    void configuredCodeStillRequiresRedisCode() {
        verificationProperties.getSms().setCode("121314");

        assertThrows(com.forest.starter.exception.BusinessException.class, () -> service.verifySmsCode(new VerifySmsCodeCommand(
            null,
            VerificationScene.LOGIN,
            "13800138000",
            "121314"
        )));

        verify(redisJsonClient).get(new RedisKey("forest:cxc-commerce:verification:sms-code:login:13800138000"), VerificationCodeValue.class);
    }

    @Test
    void configuredCodeIsStoredAndSentThroughProviderFlow() {
        verificationProperties.getSms().setCodeTtl(Duration.ofMinutes(5));
        verificationProperties.getSms().setCode("121314");
        smsProperties.setDefaultTemplateCode("SMS_DEFAULT");
        smsProperties.setVerificationTemplateCode("SMS_LOGIN");
        smsProperties.setDefaultSignName("森林");
        when(redisClient.setIfAbsent(any(RedisKey.class), eq("1"), eq(Duration.ofSeconds(60)))).thenReturn(true);
        when(redisClient.increment(any(RedisKey.class))).thenReturn(1L);
        when(smsSendService.send(any(SmsSendCommand.class))).thenReturn(new com.forest.notification.sms.service.SmsSendResult(
            "SMS202605140001",
            SmsSendStatus.SUCCESS,
            "OK",
            "短信发送成功"
        ));

        service.sendSmsCode(new SendSmsCodeCommand(
            "cxc-commerce",
            "cxc-commerce-buyer-mobile-h5",
            "MOBILE_H5",
            VerificationScene.LOGIN,
            "13800138000",
            "127.0.0.1"
        ));

        ArgumentCaptor<VerificationCodeValue> codeCaptor = ArgumentCaptor.forClass(VerificationCodeValue.class);
        verify(redisJsonClient).set(
            eq(new RedisKey("forest:cxc-commerce:verification:sms-code:login:13800138000")),
            codeCaptor.capture(),
            eq(Duration.ofMinutes(5))
        );
        assertEquals("cxc-commerce", codeCaptor.getValue().businessAppCode());
        assertEquals(sha256("121314"), codeCaptor.getValue().codeHash());

        ArgumentCaptor<SmsSendCommand> smsCommandCaptor = ArgumentCaptor.forClass(SmsSendCommand.class);
        verify(smsSendService).send(smsCommandCaptor.capture());
        assertEquals("SMS_LOGIN", smsCommandCaptor.getValue().templateCode());
        assertEquals("森林", smsCommandCaptor.getValue().signName());
        assertEquals("121314", smsCommandCaptor.getValue().templateParams().get("code"));
    }

    @Test
    void realCodeCanOnlyBeConsumedOnce() {
        VerificationCodeValue value = new VerificationCodeValue(
            "cxc-commerce",
            VerificationScene.LOGIN.name(),
            "13800138000",
            sha256("654321"),
            null
        );
        when(redisJsonClient.get(new RedisKey("forest:cxc-commerce:verification:sms-code:login:13800138000"), VerificationCodeValue.class))
            .thenReturn(Optional.of(value));
        when(redisJsonClient.getAndDelete(new RedisKey("forest:cxc-commerce:verification:sms-code:login:13800138000"), VerificationCodeValue.class))
            .thenReturn(Optional.of(value));

        VerifySmsCodeResult result = service.verifySmsCode(new VerifySmsCodeCommand(
            "cxc-commerce",
            VerificationScene.LOGIN,
            "13800138000",
            "654321"
        ));

        assertEquals(VerificationCodeMode.SMS_CODE, result.mode());
        verify(redisJsonClient).getAndDelete(new RedisKey("forest:cxc-commerce:verification:sms-code:login:13800138000"), VerificationCodeValue.class);
        verify(redisClient).delete(new RedisKey("forest:cxc-commerce:verification:sms-attempt:login:13800138000"));
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte item : hashed) {
                builder.append(String.format("%02x", item));
            }
            return "sha256$" + builder;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
