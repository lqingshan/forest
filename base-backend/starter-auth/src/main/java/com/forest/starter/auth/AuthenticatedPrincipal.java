package com.forest.starter.auth;

/**
 * 表示通过访问令牌解析得到的登录主体索引。
 */
public record AuthenticatedPrincipal(
    Long userId,
    Long accountId,
    Long sessionId,
    String accountType,
    String clientType,
    String appCode,
    String accessScope
) {
}
