package com.forest.starter.sms.aliyun;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.sms.SmsProvider;
import com.forest.starter.sms.SmsSendResult;
import com.forest.starter.sms.SmsSender;
import com.forest.starter.sms.config.ForestSmsProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Aliyun SMS implementation backed by the official Dysmsapi SDK.
 */
public class AliyunSmsSender implements SmsSender {
    private static final String OK = "OK";

    private final ForestSmsProperties properties;
    private final JsonMapper objectMapper;
    private Client client;

    public AliyunSmsSender(ForestSmsProperties properties, JsonMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public SmsSendResult send(com.forest.starter.sms.SmsSendRequest request) {
        requireText("短信手机号", request.phone());
        requireText("短信签名", request.signName());
        requireText("短信模板", request.templateCode());
        try {
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(request.phone())
                .setSignName(request.signName())
                .setTemplateCode(request.templateCode())
                .setTemplateParam(objectMapper.writeValueAsString(request.templateParams()));
            SendSmsResponse response = client().sendSms(sendSmsRequest);
            SendSmsResponseBody body = response.getBody();
            if (body == null) {
                return SmsSendResult.failed(SmsProvider.ALIYUN, null, null, "EMPTY_RESPONSE", "阿里云短信响应为空");
            }
            String code = body.getCode();
            if (OK.equals(code)) {
                return SmsSendResult.success(SmsProvider.ALIYUN, body.getRequestId(), body.getBizId(), code, body.getMessage());
            }
            return SmsSendResult.failed(SmsProvider.ALIYUN, body.getRequestId(), body.getBizId(), code, body.getMessage());
        } catch (JacksonException ex) {
            throw new BusinessException("短信模板参数序列化失败", ex);
        } catch (Exception ex) {
            throw new BusinessException("阿里云短信发送失败", ex);
        }
    }

    private Client client() throws Exception {
        if (client == null) {
            ForestSmsProperties.Aliyun aliyun = properties.getAliyun();
            requireText("阿里云短信 AccessKeyId", aliyun.getAccessKeyId());
            requireText("阿里云短信 AccessKeySecret", aliyun.getAccessKeySecret());
            requireText("阿里云短信 endpoint", aliyun.getEndpoint());
            Config config = new Config()
                .setAccessKeyId(aliyun.getAccessKeyId())
                .setAccessKeySecret(aliyun.getAccessKeySecret());
            config.endpoint = aliyun.getEndpoint();
            client = new Client(config);
        }
        return client;
    }

    private void requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(name + "不能为空");
        }
    }
}
