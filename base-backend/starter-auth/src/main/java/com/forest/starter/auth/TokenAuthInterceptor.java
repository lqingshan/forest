package com.forest.starter.auth;

import com.forest.starter.auth.context.CurrentPrincipalContext;
import com.forest.starter.auth.context.PrincipalContextHolder;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 校验需要登录的请求并写入当前认证上下文。
 */
@Component
public class TokenAuthInterceptor implements HandlerInterceptor {
    private static final String CLIENT_PREFIX = ForestApiPaths.CLIENT + "/";
    private static final String ADMIN_PREFIX = ForestApiPaths.ADMIN + "/";
    private static final String PLATFORM_PREFIX = ForestApiPaths.PLATFORM + "/";

    private final TokenAuthenticationService tokenAuthenticationService;

    public TokenAuthInterceptor(TokenAuthenticationService tokenAuthenticationService) {
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        PrincipalContextHolder.clear();
        AuthenticatedPrincipal principal = tokenAuthenticationService.authenticateAccessToken(extractToken(request));
        requireAllowedScope(request, principal.accessScope());
        PrincipalContextHolder.set(new CurrentPrincipalContext(
            principal.userId(),
            principal.accountId(),
            principal.sessionId(),
            principal.accountType(),
            principal.clientType(),
            principal.appCode(),
            principal.accessScope()
        ));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        PrincipalContextHolder.clear();
    }

    private void requireAllowedScope(HttpServletRequest request, String accessScope) {
        String path = request.getRequestURI();
        if (path.startsWith(CLIENT_PREFIX)) {
            requireScope(accessScope, "CLIENT");
        } else if (path.startsWith(ADMIN_PREFIX)) {
            requireScope(accessScope, "ADMIN");
        } else if (path.startsWith(PLATFORM_PREFIX)) {
            requireScope(accessScope, "PLATFORM");
        }
    }

    private void requireScope(String actual, String expected) {
        if (!expected.equals(actual)) {
            throw BusinessException.of("当前登录范围无权访问该接口");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
