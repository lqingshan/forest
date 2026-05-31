package com.forest.starter.sms;

import java.util.Map;

/**
 * Provider-neutral SMS send request.
 *
 * @param phone 目标手机号，通常为业务层已归一化后的手机号。
 * @param signName 短信签名，例如阿里云 SMS 的 SignName。
 * @param templateCode 短信模板编码，例如阿里云 SMS 的 TemplateCode。
 * @param templateParams 短信模板参数，验证码模板默认使用 {@code code} 作为验证码参数名。
 */
public record SmsSendRequest(
    String phone,
    String signName,
    String templateCode,
    Map<String, String> templateParams
) {
}
