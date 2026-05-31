package com.forest.starter.wechat.miniapp;

/**
 * Client interface for WeChat miniapp login and phone number APIs.
 */
public interface WechatMiniappClient {
    WechatCodeSession codeToSession(String appCode, String code);

    WechatPhoneNumber getPhoneNumber(String appCode, String phoneCode);
}
