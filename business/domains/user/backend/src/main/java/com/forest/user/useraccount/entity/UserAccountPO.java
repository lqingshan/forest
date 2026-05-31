package com.forest.user.useraccount.entity;

import com.forest.starter.jpa.ForestAuditablePO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 表示用户账号绑定持久化对象。
 */
@Entity
@Table(
    name = "user_account",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_account_user_account", columnNames = {"user_id", "account_id"}),
        @UniqueConstraint(name = "uk_user_account_account_id", columnNames = {"account_id"})
    }
)
public class UserAccountPO extends ForestAuditablePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

}
