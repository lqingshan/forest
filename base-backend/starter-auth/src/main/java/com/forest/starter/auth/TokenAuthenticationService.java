package com.forest.starter.auth;

/**
 * 定义访问令牌认证能力。
 */
public interface TokenAuthenticationService {
    AuthenticatedPrincipal authenticateAccessToken(String token);
}
