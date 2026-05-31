package com.forest.access.role.entity;

import com.forest.access.core.AccessBoundaryType;
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
    name = "access_role",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_access_role_boundary_code", columnNames = {"boundary_type", "boundary_id", "role_code"})
    }
)
public class AccessRolePO extends ForestAuditablePO {
    public enum Status {
        ACTIVE,
        DISABLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, length = 80)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 120)
    private String roleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "boundary_type", nullable = false, length = 30)
    private AccessBoundaryType boundaryType;

    @Column(name = "boundary_id", nullable = false)
    private Long boundaryId;

    @Column(name = "system_preset", nullable = false)
    private Boolean systemPreset = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    public Long getId() {
        return id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public AccessBoundaryType getBoundaryType() {
        return boundaryType;
    }

    public void setBoundaryType(AccessBoundaryType boundaryType) {
        this.boundaryType = boundaryType;
    }

    public Long getBoundaryId() {
        return boundaryId;
    }

    public void setBoundaryId(Long boundaryId) {
        this.boundaryId = boundaryId;
    }

    public Boolean getSystemPreset() {
        return systemPreset;
    }

    public void setSystemPreset(Boolean systemPreset) {
        this.systemPreset = systemPreset;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
