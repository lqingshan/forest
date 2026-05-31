package com.forest.user.auth.command;

/**
 * 表示手机号验证码登录命令。
 */
public record PhoneSmsLoginCommand(
    String phone,
    String smsCode,
    String clientType,
    String appCode,
    String accessScope,
    String ip,
    String userAgent
) {
}
