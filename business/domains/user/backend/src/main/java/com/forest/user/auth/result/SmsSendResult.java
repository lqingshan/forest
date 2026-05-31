package com.forest.user.auth.result;

/**
 * 表示登录短信验证码发送结果。
 */
public record SmsSendResult(String phone, int ttlMinutes) {
}
