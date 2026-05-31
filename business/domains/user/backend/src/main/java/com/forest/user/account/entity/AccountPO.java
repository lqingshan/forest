package com.forest.user.account.entity;

import com.forest.starter.jpa.ForestAuditablePO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 表示账号持久化对象。
 */
@Entity
@Table(
    name = "account",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_type_scope_identifier", columnNames = {"type", "credential_scope", "identifier"})
    }
)
public class AccountPO extends ForestAuditablePO {
    public static final String GLOBAL_CREDENTIAL_SCOPE = "GLOBAL";

    /**
     * 枚举账号状态。
     */
    public enum Status {
        ACTIVE,
        DISABLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "credential_scope", nullable = false, length = 80)
    private String credentialScope = GLOBAL_CREDENTIAL_SCOPE;

    @Column(nullable = false, length = 100)
    private String identifier;

    @Column(length = 255)
    private String secret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCredentialScope() {
        return credentialScope;
    }

    public void setCredentialScope(String credentialScope) {
        this.credentialScope = credentialScope;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
