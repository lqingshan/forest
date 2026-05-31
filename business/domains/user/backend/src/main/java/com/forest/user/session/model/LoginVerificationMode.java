package com.forest.user.session.model;

/**
 * 表示一次登录请求使用的身份验证方式。
 */
public enum LoginVerificationMode {
    SMS_CODE,
    PASSWORD,
    CARRIER_ONE_CLICK,
    WECHAT_OPENID,
    WECHAT_PHONE
}
