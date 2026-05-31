package com.forest.user.session.entity;

import com.forest.starter.jpa.ForestAuditablePO;
import com.forest.starter.time.ForestTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 表示一次服务端登录会话。
 */
@Entity
@Table(name = "auth_session")
public class AuthSessionPO extends ForestAuditablePO {
    public enum Status {
        ACTIVE,
        EXPIRED,
        REVOKED,
        FROZEN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_no", nullable = false, length = 64, unique = true)
    private String sessionNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_type", nullable = false, length = 30)
    private String accountType;

    @Column(name = "client_type", nullable = false, length = 30)
    private String clientType;

    @Column(name = "app_code", nullable = false, length = 60)
    private String appCode;

    @Column(name = "access_scope", nullable = false, length = 20)
    private String accessScope;

    @Column(name = "refresh_token_jti", nullable = false, length = 80)
    private String refreshTokenJti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(name = "login_ip", length = 64)
    private String loginIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;

    @Column(name = "refresh_expires_at", nullable = false)
    private LocalDateTime refreshExpiresAt;

    @PrePersist
    protected void initializeLastActiveTime() {
        if (lastActiveTime == null) {
            lastActiveTime = getCreatedTime() == null ? ForestTime.now() : getCreatedTime();
        }
    }

    public Long getId() {
        return id;
    }

    public String getSessionNo() {
        return sessionNo;
    }

    public void setSessionNo(String sessionNo) {
        this.sessionNo = sessionNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAccessScope() {
        return accessScope;
    }

    public void setAccessScope(String accessScope) {
        this.accessScope = accessScope;
    }

    public String getRefreshTokenJti() {
        return refreshTokenJti;
    }

    public void setRefreshTokenJti(String refreshTokenJti) {
        this.refreshTokenJti = refreshTokenJti;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public LocalDateTime getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    public void setRefreshExpiresAt(LocalDateTime refreshExpiresAt) {
        this.refreshExpiresAt = refreshExpiresAt;
    }

}
