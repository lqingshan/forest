package com.forest.file.entity;

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

import java.time.LocalDateTime;

/**
 * 表示一次文件直传过程。
 */
@Entity
@Table(
    name = "file_upload_session",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_file_upload_session_no", columnNames = "upload_session_no")
    }
)
public class FileUploadSessionPO extends ForestAuditablePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_session_no", nullable = false, length = 64)
    private String uploadSessionNo;

    @Column(name = "file_no", nullable = false, length = 64)
    private String fileNo;

    @Column(name = "business_app_code", nullable = false, length = 80)
    private String businessAppCode;

    @Column(name = "uploader_user_id", nullable = false)
    private Long uploaderUserId;

    @Column(name = "expected_content_type", nullable = false, length = 120)
    private String expectedContentType;

    @Column(name = "expected_size_bytes", nullable = false)
    private Long expectedSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "expected_file_category", nullable = false, length = 20)
    private FileCategory expectedFileCategory;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileUploadSessionStatus status = FileUploadSessionStatus.CREATED;

    public Long getId() {
        return id;
    }

    public String getUploadSessionNo() {
        return uploadSessionNo;
    }

    public void setUploadSessionNo(String uploadSessionNo) {
        this.uploadSessionNo = uploadSessionNo;
    }

    public String getFileNo() {
        return fileNo;
    }

    public void setFileNo(String fileNo) {
        this.fileNo = fileNo;
    }

    public String getBusinessAppCode() {
        return businessAppCode;
    }

    public void setBusinessAppCode(String businessAppCode) {
        this.businessAppCode = businessAppCode;
    }

    public Long getUploaderUserId() {
        return uploaderUserId;
    }

    public void setUploaderUserId(Long uploaderUserId) {
        this.uploaderUserId = uploaderUserId;
    }

    public String getExpectedContentType() {
        return expectedContentType;
    }

    public void setExpectedContentType(String expectedContentType) {
        this.expectedContentType = expectedContentType;
    }

    public Long getExpectedSizeBytes() {
        return expectedSizeBytes;
    }

    public void setExpectedSizeBytes(Long expectedSizeBytes) {
        this.expectedSizeBytes = expectedSizeBytes;
    }

    public FileCategory getExpectedFileCategory() {
        return expectedFileCategory;
    }

    public void setExpectedFileCategory(FileCategory expectedFileCategory) {
        this.expectedFileCategory = expectedFileCategory;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public FileUploadSessionStatus getStatus() {
        return status;
    }

    public void setStatus(FileUploadSessionStatus status) {
        this.status = status;
    }

}
