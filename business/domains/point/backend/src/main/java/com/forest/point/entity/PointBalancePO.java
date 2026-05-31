package com.forest.point.entity;

import com.forest.starter.jpa.ForestAuditablePO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 表示积分余额持久化对象。
 */
@Entity
@Table(
    name = "point_balance",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_point_balance_user_id", columnNames = {"user_id"})
    }
)
public class PointBalancePO extends ForestAuditablePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer balance = 0;

    @Column(name = "total_income", nullable = false)
    private Integer totalIncome = 0;

    @Column(name = "total_spend", nullable = false)
    private Integer totalSpend = 0;

    @Column(nullable = false)
    private Integer version = 0;

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

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(Integer totalIncome) {
        this.totalIncome = totalIncome;
    }

    public Integer getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(Integer totalSpend) {
        this.totalSpend = totalSpend;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}
