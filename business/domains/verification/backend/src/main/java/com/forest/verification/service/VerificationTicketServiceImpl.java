package com.forest.verification.service;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.redis.client.ForestRedisJsonClient;
import com.forest.starter.redis.key.RedisKey;
import com.forest.starter.redis.key.RedisKeyFactory;
import com.forest.starter.time.ForestTime;
import com.forest.verification.config.ForestVerificationProperties;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Redis-backed one-time verification ticket service.
 */
@Service
public class VerificationTicketServiceImpl implements VerificationTicketService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisKeyFactory redisKeys;
    private final ForestRedisJsonClient redisJsonClient;
    private final ForestVerificationProperties properties;

    public VerificationTicketServiceImpl(
        RedisKeyFactory redisKeys,
        ForestRedisJsonClient redisJsonClient,
        ForestVerificationProperties properties
    ) {
        this.redisKeys = redisKeys;
        this.redisJsonClient = redisJsonClient;
        this.properties = properties;
    }

    @Override
    public VerificationTicket issue(IssueVerificationTicketCommand command) {
        requireIssue(command);
        String ticketNo = nextTicketNo();
        VerificationTicket ticket = new VerificationTicket(
            ticketNo,
            command.scene().name(),
            command.businessAppCode(),
            command.clientAppCode(),
            command.clientType(),
            command.userId(),
            command.target(),
            command.targetType(),
            ForestTime.now()
        );
        redisJsonClient.set(ticketKey(command.scene(), ticketNo), ticket, properties.getSms().getTicketTtl());
        return ticket;
    }

    @Override
    public VerificationTicket consume(ConsumeVerificationTicketCommand command) {
        requireConsume(command);
        VerificationTicket ticket = redisJsonClient.getAndDelete(ticketKey(command.scene(), command.ticketNo()), VerificationTicket.class)
            .orElseThrow(() -> new BusinessException("验证凭证已失效，请重新验证"));
        requireEquals("验证凭证场景不匹配", command.scene().name(), ticket.scene());
        requireEquals("验证凭证应用不匹配", command.businessAppCode(), ticket.businessAppCode());
        requireEquals("验证凭证客户端应用不匹配", command.clientAppCode(), ticket.clientAppCode());
        requireEquals("验证凭证客户端类型不匹配", command.clientType(), ticket.clientType());
        requireEquals("验证凭证用户不匹配", command.userId(), ticket.userId());
        requireEquals("验证凭证目标不匹配", command.target(), ticket.target());
        requireEquals("验证凭证目标类型不匹配", command.targetType(), ticket.targetType());
        return ticket;
    }

    private RedisKey ticketKey(VerificationScene scene, String ticketNo) {
        return redisKeys.verificationTicket(scene.name().toLowerCase(), ticketNo);
    }

    private String nextTicketNo() {
        String date = ForestTime.now().format(DATE_FORMAT);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
        return "VT" + date + random;
    }

    private void requireIssue(IssueVerificationTicketCommand command) {
        if (command == null) {
            throw new BusinessException("验证凭证签发命令不能为空");
        }
        requireText("验证场景", command.scene() == null ? null : command.scene().name());
        requireText("业务应用编码", command.businessAppCode());
        requireText("验证目标", command.target());
        requireText("验证目标类型", command.targetType());
    }

    private void requireConsume(ConsumeVerificationTicketCommand command) {
        if (command == null) {
            throw new BusinessException("验证凭证消费命令不能为空");
        }
        requireText("验证凭证编号", command.ticketNo());
        requireText("验证场景", command.scene() == null ? null : command.scene().name());
        requireText("业务应用编码", command.businessAppCode());
        requireText("验证目标", command.target());
        requireText("验证目标类型", command.targetType());
    }

    private void requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(name + "不能为空");
        }
    }

    private void requireEquals(String message, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new BusinessException(message);
        }
    }
}
