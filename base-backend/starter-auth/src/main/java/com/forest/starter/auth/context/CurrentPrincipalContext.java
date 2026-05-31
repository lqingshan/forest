package com.forest.starter.auth.context;

/**
 * 表示当前请求中的通用登录主体上下文。
 *
 * <p>这里只保存 token 解析出的身份索引和端侧访问范围，不承载用户资料、
 * 企业、店铺、部门、角色或业务权限信息。</p>
 */
public record CurrentPrincipalContext(
    Long userId,
    Long accountId,
    Long sessionId,
    String accountType,
    String clientType,
    String appCode,
    String accessScope
) {
}
