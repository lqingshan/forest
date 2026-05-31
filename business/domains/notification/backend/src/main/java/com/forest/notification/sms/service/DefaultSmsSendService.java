package com.forest.notification.sms.service;

import com.forest.notification.sms.entity.SmsSendLogPO;
import com.forest.notification.sms.entity.SmsSendStatus;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.sms.SmsSendRequest;
import com.forest.starter.sms.SmsSender;
import com.forest.starter.sms.config.ForestSmsProperties;
import com.forest.starter.time.ForestTime;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Default SMS send orchestration with database audit log.
 */
@Service
public class DefaultSmsSendService implements SmsSendService {
    private final SmsSendLogWriter logWriter;
    private final SmsNumberGenerator numberGenerator;
    private final SmsSender smsSender;
    private final ForestSmsProperties properties;
    private final SmsSendLogContentRenderer contentRenderer;

    public DefaultSmsSendService(
        SmsSendLogWriter logWriter,
        SmsNumberGenerator numberGenerator,
        SmsSender smsSender,
        ForestSmsProperties properties,
        SmsSendLogContentRenderer contentRenderer
    ) {
        this.logWriter = logWriter;
        this.numberGenerator = numberGenerator;
        this.smsSender = smsSender;
        this.properties = properties;
        this.contentRenderer = contentRenderer;
    }

    @Override
    public SmsSendResult send(SmsSendCommand command) {
        requireCommand(command);
        SmsSendLogPO log = createPendingLog(command);
        log = logWriter.createPending(log);

        com.forest.starter.sms.SmsSendResult providerResult;
        try {
            providerResult = smsSender.send(new SmsSendRequest(
                command.phone(),
                log.getSignName(),
                log.getTemplateCode(),
                safeParams(command.templateParams())
            ));
        } catch (RuntimeException ex) {
            applyProviderFailure(log, ex);
            logWriter.saveResult(log);
            throw ex;
        }
        applyProviderResult(log, providerResult);
        log = logWriter.saveResult(log);

        if (log.getStatus() == SmsSendStatus.FAILED) {
            throw new BusinessException(log.getProviderResponseMessage() == null ? "短信发送失败" : log.getProviderResponseMessage());
        }
        return new SmsSendResult(log.getSmsNo(), log.getStatus(), log.getProviderResponseCode(), log.getProviderResponseMessage());
    }

    private SmsSendLogPO createPendingLog(SmsSendCommand command) {
        SmsSendLogPO log = new SmsSendLogPO();
        log.setSmsNo(numberGenerator.nextSmsNo());
        log.setBusinessAppCode(command.businessAppCode());
        log.setClientAppCode(command.clientAppCode());
        log.setClientType(command.clientType());
        log.setScene(command.scene());
        log.setPhone(command.phone());
        log.setTemplateCode(firstText(command.templateCode(), properties.getDefaultTemplateCode()));
        log.setSignName(firstText(command.signName(), properties.getDefaultSignName()));
        log.setContentSnapshot(contentRenderer.render(command.contentTemplate(), safeParams(command.templateParams()), properties.getLog()));
        log.setProvider(properties.getProvider().name());
        log.setSendIp(command.sendIp());
        return log;
    }

    private void applyProviderResult(SmsSendLogPO log, com.forest.starter.sms.SmsSendResult result) {
        log.setProvider(result.provider().name());
        log.setProviderRequestId(result.requestId());
        log.setProviderBizId(result.bizId());
        log.setProviderResponseCode(result.responseCode());
        log.setProviderResponseMessage(result.responseMessage());
        if (result.success()) {
            log.setStatus(SmsSendStatus.SUCCESS);
            log.setSentAt(ForestTime.now());
            return;
        }
        log.setStatus(SmsSendStatus.FAILED);
        log.setFailedAt(ForestTime.now());
    }

    private void applyProviderFailure(SmsSendLogPO log, RuntimeException ex) {
        log.setStatus(SmsSendStatus.FAILED);
        log.setFailedAt(ForestTime.now());
        log.setProviderResponseCode("EXCEPTION");
        log.setProviderResponseMessage(ex.getMessage());
    }

    private void requireCommand(SmsSendCommand command) {
        if (command == null) {
            throw new BusinessException("短信发送命令不能为空");
        }
        requireText("业务应用编码", command.businessAppCode());
        requireText("短信场景", command.scene());
        requireText("手机号", command.phone());
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

    private Map<String, String> safeParams(Map<String, String> params) {
        return params == null ? Map.of() : params;
    }
}
