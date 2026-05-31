package com.forest.organization.core.entity;

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
 * Enterprise organization, also used as the tenant boundary in the first phase.
 */
@Entity
@Table(
    name = "organization",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_organization_no", columnNames = "organization_no")
    }
)
public class OrganizationPO extends ForestAuditablePO {
    public enum Status {
        ACTIVE,
        DISABLED
    }

    public enum CertificationStatus {
        NOT_SUBMITTED,
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_no", nullable = false, length = 64)
    private String organizationNo;

    @Column(name = "organization_name", nullable = false, length = 150)
    private String organizationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "certification_status", nullable = false, length = 20)
    private CertificationStatus certificationStatus = CertificationStatus.NOT_SUBMITTED;

    @Column(name = "current_certification_id")
    private Long currentCertificationId;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganizationNo() {
        return organizationNo;
    }

    public void setOrganizationNo(String organizationNo) {
        this.organizationNo = organizationNo;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public CertificationStatus getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(CertificationStatus certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public Long getCurrentCertificationId() {
        return currentCertificationId;
    }

    public void setCurrentCertificationId(Long currentCertificationId) {
        this.currentCertificationId = currentCertificationId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}
