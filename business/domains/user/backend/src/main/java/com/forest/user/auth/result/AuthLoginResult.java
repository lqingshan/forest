package com.forest.user.auth.result;

/**
 * 表示一次登录成功后的令牌结果。
 */
public record AuthLoginResult(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    long refreshExpiresIn,
    String clientType,
    String appCode,
    String accessScope,
    boolean firstLogin
) {
}
