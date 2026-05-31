package com.forest.user.auth.command;

/**
 * 表示 APP 原生本机号一键登录命令。
 */
public record CarrierOneClickLoginCommand(
    String carrierToken,
    String provider,
    String clientType,
    String appCode,
    String accessScope,
    String ip,
    String userAgent
) {
}
