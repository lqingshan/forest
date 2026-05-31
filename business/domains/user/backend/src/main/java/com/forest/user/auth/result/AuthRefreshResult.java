package com.forest.user.auth.result;

/**
 * 表示 refreshToken 换取 accessToken 的结果。
 */
public record AuthRefreshResult(
    String accessToken,
    String tokenType,
    long expiresIn,
    String clientType,
    String appCode,
    String accessScope
) {
}
