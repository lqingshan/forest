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
 * 表示文件元数据持久化对象。
 */
@Entity
@Table(
    name = "file_object",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_file_object_file_no", columnNames = "file_no"),
        @UniqueConstraint(name = "uk_file_object_object_key", columnNames = {"bucket", "object_key"})
    }
)
public class FileObjectPO extends ForestAuditablePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_no", nullable = false, length = 64)
    private String fileNo;

    @Column(name = "business_app_code", nullable = false, length = 80)
    private String businessAppCode;

    @Column(name = "uploaded_client_app_code", nullable = false, length = 80)
    private String uploadedClientAppCode;

    @Column(name = "uploader_user_id", nullable = false)
    private Long uploaderUserId;

    @Column(name = "uploader_account_id", nullable = false)
    private Long uploaderAccountId;

    @Column(nullable = false, length = 128)
    private String bucket;

    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Column(length = 128)
    private String etag;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(length = 32)
    private String extension;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_category", nullable = false, length = 20)
    private FileCategory fileCategory;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(length = 64)
    private String sha256;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileStatus status = FileStatus.UPLOADING;

    @Column(name = "deleted_time")
    private LocalDateTime deletedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUploadedClientAppCode() {
        return uploadedClientAppCode;
    }

    public void setUploadedClientAppCode(String uploadedClientAppCode) {
        this.uploadedClientAppCode = uploadedClientAppCode;
    }

    public Long getUploaderUserId() {
        return uploaderUserId;
    }

    public void setUploaderUserId(Long uploaderUserId) {
        this.uploaderUserId = uploaderUserId;
    }

    public Long getUploaderAccountId() {
        return uploaderAccountId;
    }

    public void setUploaderAccountId(Long uploaderAccountId) {
        this.uploaderAccountId = uploaderAccountId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public FileCategory getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(FileCategory fileCategory) {
        this.fileCategory = fileCategory;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    public LocalDateTime getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(LocalDateTime deletedTime) {
        this.deletedTime = deletedTime;
    }
}
