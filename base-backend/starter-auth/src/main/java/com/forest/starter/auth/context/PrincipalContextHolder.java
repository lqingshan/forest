package com.forest.starter.auth.context;

import com.forest.starter.exception.BusinessException;

/**
 * 保存当前请求登录主体上下文。
 */
public final class PrincipalContextHolder {
    private static final ThreadLocal<CurrentPrincipalContext> CURRENT_CONTEXT = new ThreadLocal<>();

    private PrincipalContextHolder() {
    }

    public static void set(CurrentPrincipalContext context) {
        CURRENT_CONTEXT.set(context);
    }

    public static CurrentPrincipalContext get() {
        return CURRENT_CONTEXT.get();
    }

    public static CurrentPrincipalContext require() {
        return require("未登录");
    }

    public static CurrentPrincipalContext require(String message) {
        CurrentPrincipalContext context = CURRENT_CONTEXT.get();
        if (context == null) {
            throw new BusinessException(message);
        }
        return context;
    }

    public static Long getUserId() {
        CurrentPrincipalContext context = CURRENT_CONTEXT.get();
        return context == null ? null : context.userId();
    }

    public static void clear() {
        CURRENT_CONTEXT.remove();
    }
}
