package com.forest.starter.wechat.miniapp;

/**
 * Result of WeChat miniapp phone number authorization.
 */
public record WechatPhoneNumber(
    String phoneNumber,
    String purePhoneNumber,
    String countryCode
) {
}
