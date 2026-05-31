package com.forest.access.role.entity;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.core.AccessSubjectType;
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
    name = "access_role_assignment",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_access_role_assignment",
            columnNames = {"subject_type", "subject_id", "boundary_type", "boundary_id", "role_id"}
        )
    }
)
public class AccessRoleAssignmentPO extends ForestAuditablePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 30)
    private AccessSubjectType subjectType;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "boundary_type", nullable = false, length = 30)
    private AccessBoundaryType boundaryType;

    @Column(name = "boundary_id", nullable = false)
    private Long boundaryId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    public Long getId() {
        return id;
    }

    public AccessSubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(AccessSubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
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

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
