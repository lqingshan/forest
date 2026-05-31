package com.forest.verification.service;

import com.forest.notification.sms.service.SmsSendCommand;
import com.forest.notification.sms.service.SmsSendService;
import com.forest.notification.sms.entity.SmsSendStatus;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.redis.client.ForestRedisClient;
import com.forest.starter.redis.client.ForestRedisJsonClient;
import com.forest.starter.redis.key.RedisKey;
import com.forest.starter.redis.key.RedisKeyFactory;
import com.forest.starter.sms.config.ForestSmsProperties;
import com.forest.starter.time.ForestTime;
import com.forest.verification.config.ForestVerificationProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Redis-backed verification code service.
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisKeyFactory redisKeys;
    private final ForestRedisClient redisClient;
    private final ForestRedisJsonClient redisJsonClient;
    private final SmsSendService smsSendService;
    private final ForestVerificationProperties verificationProperties;
    private final ForestSmsProperties smsProperties;
    private final SecureRandom random = new SecureRandom();

    public VerificationCodeServiceImpl(
        RedisKeyFactory redisKeys,
        ForestRedisClient redisClient,
        ForestRedisJsonClient redisJsonClient,
        SmsSendService smsSendService,
        ForestVerificationProperties verificationProperties,
        ForestSmsProperties smsProperties
    ) {
        this.redisKeys = redisKeys;
        this.redisClient = redisClient;
        this.redisJsonClient = redisJsonClient;
        this.smsSendService = smsSendService;
        this.verificationProperties = verificationProperties;
        this.smsProperties = smsProperties;
    }

    @Override
    public SendSmsCodeResult sendSmsCode(SendSmsCodeCommand command) {
        requireSendCommand(command);
        ForestVerificationProperties.Sms sms = verificationProperties.getSms();
        RedisKey sendLimitKey = redisKeys.verificationSmsSendLimit(command.scene().name().toLowerCase(), command.phone());
        if (!redisClient.setIfAbsent(sendLimitKey, "1", sms.getSendInterval())) {
            throw new BusinessException("验证码发送过于频繁");
        }
        acquireDailyLimit(command, sms);

        String code = verificationCode(sms);
        String businessAppCode = businessAppCode(command.businessAppCode());
        RedisKey codeKey = redisKeys.verificationSmsCode(command.scene().name().toLowerCase(), command.phone());
        redisJsonClient.set(
            codeKey,
            new VerificationCodeValue(businessAppCode, command.scene().name(), command.phone(), hash(code), ForestTime.now()),
            sms.getCodeTtl()
        );
        try {
            com.forest.notification.sms.service.SmsSendResult sendResult = smsSendService.send(new SmsSendCommand(
                businessAppCode,
                command.clientAppCode(),
                command.clientType(),
                command.scene().name(),
                command.phone(),
                firstText(smsProperties.getVerificationTemplateCode(), smsProperties.getDefaultTemplateCode()),
                smsProperties.getDefaultSignName(),
                Map.of("code", code),
                smsProperties.getVerificationContentTemplate(),
                command.sendIp()
            ));
            if (sendResult.status() != SmsSendStatus.SUCCESS) {
                throw new BusinessException(sendResult.providerResponseMessage() == null ? "短信发送失败" : sendResult.providerResponseMessage());
            }
        } catch (RuntimeException ex) {
            redisClient.delete(codeKey);
            throw ex;
        }
        return new SendSmsCodeResult(command.phone(), sms.getCodeTtl());
    }

    @Override
    public VerifySmsCodeResult verifySmsCode(VerifySmsCodeCommand command) {
        requireVerifyCommand(command);
        ForestVerificationProperties.Sms sms = verificationProperties.getSms();

        RedisKey codeKey = redisKeys.verificationSmsCode(command.scene().name().toLowerCase(), command.phone());
        VerificationCodeValue value = redisJsonClient.get(codeKey, VerificationCodeValue.class)
            .orElseThrow(() -> new BusinessException("验证码不存在或已过期"));
        if (!businessAppCode(command.businessAppCode()).equals(value.businessAppCode()) || !command.scene().name().equals(value.scene())) {
            throw new BusinessException("验证码不存在或已过期");
        }
        if (!hash(command.code()).equals(value.codeHash())) {
            recordFailedAttempt(command, sms);
            throw new BusinessException("验证码错误");
        }
        redisJsonClient.getAndDelete(codeKey, VerificationCodeValue.class)
            .orElseThrow(() -> new BusinessException("验证码不存在或已过期"));
        redisClient.delete(redisKeys.verificationSmsAttempt(command.scene().name().toLowerCase(), command.phone()));
        return new VerifySmsCodeResult(command.phone(), VerificationCodeMode.SMS_CODE);
    }

    private void acquireDailyLimit(SendSmsCodeCommand command, ForestVerificationProperties.Sms sms) {
        if (sms.getDailyLimit() <= 0) {
            throw new BusinessException("短信每日发送上限配置错误");
        }
        String date = ForestTime.now().format(DATE_FORMAT);
        RedisKey key = redisKeys.verificationSmsDailyLimit(command.scene().name().toLowerCase(), command.phone(), date);
        long current = redisClient.increment(key);
        if (current == 1L) {
            redisClient.expire(key, Duration.ofDays(1));
        }
        if (current > sms.getDailyLimit()) {
            throw new BusinessException("验证码发送次数已达今日上限");
        }
    }

    private void recordFailedAttempt(VerifySmsCodeCommand command, ForestVerificationProperties.Sms sms) {
        RedisKey key = redisKeys.verificationSmsAttempt(command.scene().name().toLowerCase(), command.phone());
        long current = redisClient.increment(key);
        if (current == 1L) {
            redisClient.expire(key, sms.getCodeTtl());
        }
        if (current > sms.getMaxAttempts()) {
            throw new BusinessException("验证码尝试次数过多");
        }
    }

    /**
     * 获取本次短信验证码。
     *
     * <p>当前项目约定 local/mock 阶段使用配置中的固定验证码，便于测试人员从
     * sms_send_log 中读取并完成完整 Redis 校验链路。若后续需要随机验证码，可把
     * 配置留空并启用这里的随机兜底。</p>
     */
    private String verificationCode(ForestVerificationProperties.Sms sms) {
        if (sms.getCode() != null && !sms.getCode().isBlank()) {
            return sms.getCode().trim();
        }
        return generateCode();
    }

    private String generateCode() {
        int value = random.nextInt(900000) + 100000;
        return String.format("%06d", value);
    }

    private String hash(String rawCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawCode.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte item : hashed) {
                builder.append(String.format("%02x", item));
            }
            return "sha256$" + builder;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", ex);
        }
    }

    private void requireSendCommand(SendSmsCodeCommand command) {
        if (command == null) {
            throw new BusinessException("短信验证码发送命令不能为空");
        }
        requireText("短信场景", command.scene() == null ? null : command.scene().name());
        requireText("手机号", command.phone());
    }

    private void requireVerifyCommand(VerifySmsCodeCommand command) {
        if (command == null) {
            throw new BusinessException("短信验证码校验命令不能为空");
        }
        requireText("短信场景", command.scene() == null ? null : command.scene().name());
        requireText("手机号", command.phone());
        requireText("验证码", command.code());
    }

    private void requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(name + "不能为空");
        }
    }

    private String firstText(String value, String fallback) {
        if (value != null && !value.isBlank()) {
            return value;
        }
        return fallback;
    }

    private String businessAppCode(String value) {
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return redisKeys.appCode();
    }
}
