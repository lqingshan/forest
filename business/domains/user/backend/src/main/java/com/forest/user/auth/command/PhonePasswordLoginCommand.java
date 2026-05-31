package com.forest.user.auth.command;

/**
 * 表示手机号密码登录命令。
 */
public record PhonePasswordLoginCommand(
    String phone,
    String password,
    String clientType,
    String appCode,
    String accessScope,
    String ip,
    String userAgent
) {
}
