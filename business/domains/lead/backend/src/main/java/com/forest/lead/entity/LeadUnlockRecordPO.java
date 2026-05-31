package com.forest.lead.entity;

import com.forest.starter.jpa.ForestAuditablePO;
import com.forest.starter.time.ForestTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * 表示线索解锁记录持久化对象。
 */
@Entity
@Table(
    name = "lead_unlock_record",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_lead_unlock_user_lead", columnNames = {"user_id", "lead_id"})
    }
)
public class LeadUnlockRecordPO extends ForestAuditablePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "point_cost", nullable = false)
    private Integer pointCost;

    @Column(name = "unlock_time", nullable = false)
    private LocalDateTime unlockTime;

    @PrePersist
    protected void initializeUnlockTime() {
        if (unlockTime == null) {
            unlockTime = getCreatedTime() == null ? ForestTime.now() : getCreatedTime();
        }
    }

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

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public Integer getPointCost() {
        return pointCost;
    }

    public void setPointCost(Integer pointCost) {
        this.pointCost = pointCost;
    }

    public LocalDateTime getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(LocalDateTime unlockTime) {
        this.unlockTime = unlockTime;
    }

}
