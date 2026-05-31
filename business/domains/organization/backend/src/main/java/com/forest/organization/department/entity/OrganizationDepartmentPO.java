package com.forest.organization.department.entity;

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
 * Department inside an organization.
 */
@Entity
@Table(
    name = "organization_department",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_organization_department_no", columnNames = "department_no")
    }
)
public class OrganizationDepartmentPO extends ForestAuditablePO {
    public enum Status {
        ACTIVE,
        DISABLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_no", nullable = false, length = 64)
    private String departmentNo;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "department_name", nullable = false, length = 120)
    private String departmentName;

    @Column(name = "default_department", nullable = false)
    private Boolean defaultDepartment = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartmentNo() {
        return departmentNo;
    }

    public void setDepartmentNo(String departmentNo) {
        this.departmentNo = departmentNo;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Boolean getDefaultDepartment() {
        return defaultDepartment;
    }

    public void setDefaultDepartment(Boolean defaultDepartment) {
        this.defaultDepartment = defaultDepartment;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
