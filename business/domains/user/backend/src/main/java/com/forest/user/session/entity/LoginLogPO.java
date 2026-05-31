package com.forest.user.session.entity;

import com.forest.starter.jpa.ForestAuditablePO;
import com.forest.user.session.model.LoginVerificationMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 表示登录和刷新相关审计日志。
 */
@Entity
@Table(name = "login_log")
public class LoginLogPO extends ForestAuditablePO {
    public enum Result {
        SUCCESS,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "account_type", length = 30)
    private String accountType;

    @Column(name = "identifier_snapshot", length = 120)
    private String identifierSnapshot;

    @Column(name = "phone_snapshot", length = 30)
    private String phoneSnapshot;

    @Column(name = "client_type", length = 30)
    private String clientType;

    @Column(name = "app_code", length = 60)
    private String appCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Result result;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_mode", length = 30)
    private LoginVerificationMode verificationMode;

    @Column(name = "login_ip", length = 64)
    private String loginIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    public Long getId() {
        return id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setIdentifierSnapshot(String identifierSnapshot) {
        this.identifierSnapshot = identifierSnapshot;
    }

    public void setPhoneSnapshot(String phoneSnapshot) {
        this.phoneSnapshot = phoneSnapshot;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setVerificationMode(LoginVerificationMode verificationMode) {
        this.verificationMode = verificationMode;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LoginVerificationMode getVerificationMode() {
        return verificationMode;
    }

    public Result getResult() {
        return result;
    }
}
