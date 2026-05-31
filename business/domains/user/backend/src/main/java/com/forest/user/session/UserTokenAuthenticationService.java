package com.forest.user.session;

import com.forest.starter.auth.AuthenticatedPrincipal;
import com.forest.starter.auth.TokenAuthenticationService;
import com.forest.starter.exception.BusinessException;
import com.forest.user.account.service.AccountService;
import com.forest.user.account.token.JwtTokenProvider;
import com.forest.user.session.entity.AuthSessionPO;
import com.forest.user.session.service.AuthSessionService;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 实现基于用户业务的访问令牌认证能力。
 */
@Service
public class UserTokenAuthenticationService implements TokenAuthenticationService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountService accountService;
    private final UserService userService;
    private final AuthSessionService authSessionService;

    public UserTokenAuthenticationService(
        JwtTokenProvider jwtTokenProvider,
        AccountService accountService,
        UserService userService,
        AuthSessionService authSessionService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountService = accountService;
        this.userService = userService;
        this.authSessionService = authSessionService;
    }

    @Override
    public AuthenticatedPrincipal authenticateAccessToken(String token) {
        if (token == null || !jwtTokenProvider.validateToken(token) || jwtTokenProvider.isRefreshToken(token)) {
            throw new BusinessException("未登录或 Token 无效");
        }

        String sessionNo = jwtTokenProvider.getSessionNoFromToken(token);
        String clientType = jwtTokenProvider.getClientTypeFromToken(token);
        String appCode = jwtTokenProvider.getAppCodeFromToken(token);
        String accessScope = jwtTokenProvider.getAccessScopeFromToken(token);

        AuthSessionPO session = authSessionService.requireActiveBySessionNo(sessionNo);
        if (!session.getClientType().equals(clientType)
            || !session.getAppCode().equals(appCode)
            || !session.getAccessScope().equals(accessScope)) {
            throw new BusinessException("会话不匹配");
        }

        accountService.getRequiredById(session.getAccountId());
        UserPO user = userService.getRequiredActiveById(session.getUserId());

        return new AuthenticatedPrincipal(
            user.getId(),
            session.getAccountId(),
            session.getId(),
            session.getAccountType(),
            clientType,
            appCode,
            accessScope
        );
    }
}
