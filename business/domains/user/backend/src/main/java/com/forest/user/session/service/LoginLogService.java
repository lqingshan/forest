package com.forest.user.session.service;

import com.forest.user.account.entity.AccountPO;
import com.forest.user.session.entity.AuthSessionPO;
import com.forest.user.session.entity.LoginLogPO;
import com.forest.user.session.model.LoginVerificationMode;
import com.forest.user.session.repository.LoginLogRepository;
import com.forest.user.user.entity.UserPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 记录登录相关审计日志。
 */
@Service
public class LoginLogService {
    private static final Logger logger = LoggerFactory.getLogger(LoginLogService.class);

    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(
        UserPO user,
        AccountPO account,
        AuthSessionPO session,
        String identifierSnapshot,
        String phoneSnapshot,
        LoginVerificationMode verificationMode,
        LoginRequestContext context
    ) {
        LoginLogPO log = baseLog(account, identifierSnapshot, phoneSnapshot, context);
        log.setUserId(user.getId());
        log.setAccountId(account.getId());
        log.setSessionId(session.getId());
        log.setResult(LoginLogPO.Result.SUCCESS);
        log.setVerificationMode(verificationMode);
        loginLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(
        String accountType,
        String identifierSnapshot,
        String phoneSnapshot,
        LoginVerificationMode verificationMode,
        String failureReason,
        LoginRequestContext context
    ) {
        LoginLogPO log = baseLog(null, identifierSnapshot, phoneSnapshot, context);
        log.setAccountType(accountType);
        log.setResult(LoginLogPO.Result.FAILED);
        log.setFailureReason(failureReason);
        log.setVerificationMode(verificationMode);
        loginLogRepository.save(log);
        logger.warn(
            "Login failed. accountType={}, verificationMode={}, appCode={}, clientType={}, reason={}",
            accountType,
            verificationMode,
            context.safeAppCode(),
            context.safeClientType(),
            failureReason
        );
    }

    private LoginLogPO baseLog(
        AccountPO account,
        String identifierSnapshot,
        String phoneSnapshot,
        LoginRequestContext context
    ) {
        LoginLogPO log = new LoginLogPO();
        if (account != null) {
            log.setAccountType(account.getType());
        }
        log.setIdentifierSnapshot(identifierSnapshot);
        log.setPhoneSnapshot(phoneSnapshot);
        log.setClientType(context.safeClientType());
        log.setAppCode(context.safeAppCode());
        log.setLoginIp(context.ip());
        log.setUserAgent(context.userAgent());
        return log;
    }

}
