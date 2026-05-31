package com.forest.user.session.service;

import com.forest.starter.auth.AccessScope;
import com.forest.starter.auth.ClientType;
import com.forest.starter.exception.BusinessException;

/**
 * 表示一次登录请求的技术上下文。
 */
public record LoginRequestContext(
    String clientType,
    String appCode,
    String accessScope,
    String ip,
    String userAgent
) {
    public String safeClientType() {
        if (clientType == null || clientType.isBlank()) {
            throw new BusinessException("clientType 不能为空");
        }
        String safeClientType = clientType.trim();
        try {
            ClientType.valueOf(safeClientType);
            return safeClientType;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("clientType 不合法");
        }
    }

    public String safeAppCode() {
        if (appCode == null || appCode.isBlank()) {
            throw new BusinessException("appCode 不能为空");
        }
        return appCode.trim();
    }

    public String safeAccessScope() {
        if (accessScope == null || accessScope.isBlank()) {
            throw new BusinessException("accessScope 不能为空");
        }
        String safeAccessScope = accessScope.trim();
        try {
            AccessScope.valueOf(safeAccessScope);
            return safeAccessScope;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("accessScope 不合法");
        }
    }
}
