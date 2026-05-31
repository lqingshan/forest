package com.forest.starter.jpa;

import com.forest.starter.time.ForestTime;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * 项目业务 PO 的统一审计字段基类。
 *
 * <p>只承载数据库规范中的固定审计字段，不承载 id、租户、组织、业务状态等领域字段。
 * createdId / modifiedId 由业务服务按场景显式写入，不在基础层读取登录上下文。</p>
 */
@MappedSuperclass
public abstract class ForestAuditablePO {
    /**
     * 统一软删除状态。
     *
     * <p>数据库字段仍然使用 integer，业务代码通过枚举获取持久化值，避免在各模块散落
     * {@code 0}/{@code 1} 这类魔法值。</p>
     */
    public enum Deleted {
        ACTIVE(0),
        DELETED(1);

        private final Integer value;

        Deleted(Integer value) {
            this.value = value;
        }

        public Integer value() {
            return value;
        }

        public boolean matches(Integer deleted) {
            return value.equals(deleted);
        }
    }

    @Column(name = "created_id")
    private Long createdId;

    @Column(name = "modified_id")
    private Long modifiedId;

    @Column(nullable = false)
    private Integer deleted = Deleted.ACTIVE.value();

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = ForestTime.now();
        modifiedTime = createdTime;
        if (deleted == null) {
            deleted = Deleted.ACTIVE.value();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedTime = ForestTime.now();
    }

    public Long getCreatedId() {
        return createdId;
    }

    public void setCreatedId(Long createdId) {
        this.createdId = createdId;
    }

    public Long getModifiedId() {
        return modifiedId;
    }

    public void setModifiedId(Long modifiedId) {
        this.modifiedId = modifiedId;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
