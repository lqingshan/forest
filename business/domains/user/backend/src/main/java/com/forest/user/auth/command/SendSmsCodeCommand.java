package com.forest.user.auth.command;

/**
 * 表示发送登录短信验证码的命令。
 */
public record SendSmsCodeCommand(
    String phone,
    String clientType,
    String appCode,
    String accessScope,
    String ip
) {
}
