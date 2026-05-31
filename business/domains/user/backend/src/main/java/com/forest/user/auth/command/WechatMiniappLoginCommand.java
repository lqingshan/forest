package com.forest.user.auth.command;

/**
 * 表示微信小程序直接登录命令。
 */
public record WechatMiniappLoginCommand(
    String code,
    String clientType,
    String appCode,
    String accessScope,
    String ip,
    String userAgent
) {
}
