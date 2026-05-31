package com.forest.user.auth.service;

/**
 * 表示签发出的访问令牌和刷新令牌。
 */
public record AuthTokenPair(String accessToken, String refreshToken) {
}
