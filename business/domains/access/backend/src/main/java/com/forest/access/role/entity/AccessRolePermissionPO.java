package com.forest.access.role.entity;

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

@Entity
@Table(
    name = "access_role_permission",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_access_role_permission_pattern", columnNames = {"role_id", "permission_pattern"})
    }
)
public class AccessRolePermissionPO extends ForestAuditablePO {
    public enum PatternType {
        EXACT,
        WILDCARD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_pattern", nullable = false, length = 160)
    private String permissionPattern;

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_type", nullable = false, length = 20)
    private PatternType patternType;

    public Long getId() {
        return id;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getPermissionPattern() {
        return permissionPattern;
    }

    public void setPermissionPattern(String permissionPattern) {
        this.permissionPattern = permissionPattern;
    }

    public PatternType getPatternType() {
        return patternType;
    }

    public void setPatternType(PatternType patternType) {
        this.patternType = patternType;
    }
}
