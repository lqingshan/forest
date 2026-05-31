package com.forest.user.auth.command;

/**
 * 表示微信小程序手机号授权登录命令。
 */
public record WechatMiniappPhoneLoginCommand(
    String code,
    String phoneCode,
    String clientType,
    String appCode,
    String accessScope,
    String ip,
    String userAgent
) {
}
