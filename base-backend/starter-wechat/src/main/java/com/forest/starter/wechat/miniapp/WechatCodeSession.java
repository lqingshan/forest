package com.forest.starter.wechat.miniapp;

/**
 * Result of WeChat miniapp jscode2session.
 */
public record WechatCodeSession(
    String openId,
    String unionId,
    String sessionKey
) {
}
