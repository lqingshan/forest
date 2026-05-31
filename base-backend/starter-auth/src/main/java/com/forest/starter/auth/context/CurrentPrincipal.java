package com.forest.starter.auth.context;

import org.springframework.stereotype.Component;

/**
 * 提供当前请求登录主体的注入式入口。
 */
@Component
public class CurrentPrincipal {
    public CurrentPrincipalContext get() {
        return PrincipalContextHolder.get();
    }

    public CurrentPrincipalContext require() {
        return PrincipalContextHolder.require();
    }

    public CurrentPrincipalContext require(String message) {
        return PrincipalContextHolder.require(message);
    }

    public Long getUserId() {
        return PrincipalContextHolder.getUserId();
    }

    public Long requireUserId() {
        return require().userId();
    }

    public Long requireUserId(String message) {
        return require(message).userId();
    }
}
