package com.forest.file.service.impl;

import com.forest.file.config.ForestFileProperties;
import com.forest.file.entity.FileObjectPO;
import com.forest.file.entity.FileStatus;
import com.forest.file.entity.FileUploadSessionPO;
import com.forest.file.entity.FileUploadSessionStatus;
import com.forest.file.repository.FileObjectRepository;
import com.forest.file.repository.FileUploadSessionRepository;
import com.forest.file.service.FileAccessPolicy;
import com.forest.file.service.FileNumberGenerator;
import com.forest.file.service.FileObjectKeyBuilder;
import com.forest.file.service.FileService;
import com.forest.file.service.FileTypePolicy;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.auth.context.CurrentPrincipalContext;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.objectstorage.ObjectStorageBucketResolver;
import com.forest.starter.objectstorage.ObjectStorageClient;
import com.forest.starter.objectstorage.ObjectStorageDownloadRequest;
import com.forest.starter.objectstorage.ObjectStorageDownloadUrl;
import com.forest.starter.objectstorage.ObjectStorageObjectMetadata;
import com.forest.starter.objectstorage.ObjectStorageUploadCredential;
import com.forest.starter.objectstorage.ObjectStorageUploadRequest;
import com.forest.starter.time.ForestTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 文件业务服务默认实现。
 */
@Service
public class FileServiceImpl implements FileService {
    private final FileObjectRepository fileObjectRepository;
    private final FileUploadSessionRepository uploadSessionRepository;
    private final ObjectStorageClient objectStorageClient;
    private final ObjectStorageBucketResolver bucketResolver;
    private final ForestFileProperties properties;
    private final CurrentPrincipal currentAuth;
    private final FileAccessPolicy accessPolicy;
    private final FileNumberGenerator numberGenerator;
    private final FileObjectKeyBuilder objectKeyBuilder;
    private final FileTypePolicy typePolicy;

    public FileServiceImpl(
        FileObjectRepository fileObjectRepository,
        FileUploadSessionRepository uploadSessionRepository,
        ObjectStorageClient objectStorageClient,
        ObjectStorageBucketResolver bucketResolver,
        ForestFileProperties properties,
        CurrentPrincipal currentAuth,
        FileAccessPolicy accessPolicy,
        FileNumberGenerator numberGenerator,
        FileObjectKeyBuilder objectKeyBuilder,
        FileTypePolicy typePolicy
    ) {
        this.fileObjectRepository = fileObjectRepository;
        this.uploadSessionRepository = uploadSessionRepository;
        this.objectStorageClient = objectStorageClient;
        this.bucketResolver = bucketResolver;
        this.properties = properties;
        this.currentAuth = currentAuth;
        this.accessPolicy = accessPolicy;
        this.numberGenerator = numberGenerator;
        this.objectKeyBuilder = objectKeyBuilder;
        this.typePolicy = typePolicy;
    }

    @Override
    @Transactional
    public UploadSessionResult createUploadSession(CreateUploadSessionCommand command) {
        CurrentPrincipalContext auth = currentAuth.require("用户未登录");
        String businessAppCode = requireBusinessAppCode();
        String clientAppCode = requireText(auth.appCode(), "客户端 appCode 不能为空");
        String contentType = typePolicy.normalizeContentType(command.fileCategory(), command.contentType());
        typePolicy.validateSize(command.fileCategory(), command.sizeBytes());

        String fileNo = nextFileNo();
        String uploadSessionNo = nextUploadSessionNo();
        String bucket = bucketResolver.requireBucket(businessAppCode);
        String objectKey = objectKeyBuilder.build(properties.getEnv(), command.fileCategory(), fileNo, command.originalName());
        LocalDateTime expiresAt = ForestTime.now().plus(safeDuration(properties.getUploadSessionTtl(), Duration.ofMinutes(15)));

        FileObjectPO file = new FileObjectPO();
        file.setFileNo(fileNo);
        file.setBusinessAppCode(businessAppCode);
        file.setUploadedClientAppCode(clientAppCode);
        file.setUploaderUserId(auth.userId());
        file.setUploaderAccountId(auth.accountId());
        file.setBucket(bucket);
        file.setObjectKey(objectKey);
        file.setOriginalName(requireText(command.originalName(), "文件名不能为空"));
        file.setExtension(objectKeyBuilder.extension(command.originalName()));
        file.setContentType(contentType);
        file.setFileCategory(command.fileCategory());
        file.setSizeBytes(command.sizeBytes());
        file.setSha256(normalizeBlank(command.sha256()));
        file.setImageWidth(command.imageWidth());
        file.setImageHeight(command.imageHeight());
        file.setStatus(FileStatus.UPLOADING);
        file.setCreatedId(auth.userId());
        file.setModifiedId(auth.userId());
        file = fileObjectRepository.save(file);

        FileUploadSessionPO uploadSession = new FileUploadSessionPO();
        uploadSession.setUploadSessionNo(uploadSessionNo);
        uploadSession.setFileNo(fileNo);
        uploadSession.setBusinessAppCode(businessAppCode);
        uploadSession.setUploaderUserId(auth.userId());
        uploadSession.setExpectedContentType(contentType);
        uploadSession.setExpectedSizeBytes(command.sizeBytes());
        uploadSession.setExpectedFileCategory(command.fileCategory());
        uploadSession.setExpiresAt(expiresAt);
        uploadSession.setStatus(FileUploadSessionStatus.CREATED);
        uploadSession.setCreatedId(auth.userId());
        uploadSession.setModifiedId(auth.userId());
        uploadSessionRepository.save(uploadSession);

        ObjectStorageUploadCredential credential = objectStorageClient.createUploadCredential(new ObjectStorageUploadRequest(
            bucket,
            objectKey,
            contentType,
            command.sizeBytes(),
            toInstant(expiresAt)
        ));

        return new UploadSessionResult(
            uploadSessionNo,
            fileNo,
            bucket,
            objectKey,
            new UploadCredentialVO(
                credential.uploadUrl(),
                credential.method(),
                credential.formFields(),
                credential.headers(),
                toLocalDateTime(credential.expiresAt())
            ),
            toFileInfo(file)
        );
    }

    @Override
    @Transactional
    public FileInfo completeUploadSession(String uploadSessionNo) {
        CurrentPrincipalContext auth = currentAuth.require("用户未登录");
        FileUploadSessionPO uploadSession = requireUploadSession(uploadSessionNo);
        FileObjectPO file = requireFile(uploadSession.getFileNo());
        requireSameUploader(auth, file);
        requireUploadSessionUsable(uploadSession, auth.userId());
        requireBusinessApp(file);

        ObjectStorageObjectMetadata metadata = objectStorageClient.headObject(file.getBucket(), file.getObjectKey());
        validateUploadedObject(uploadSession, metadata);

        file.setEtag(metadata.etag());
        file.setStatus(FileStatus.AVAILABLE);
        file.setModifiedId(auth.userId());
        uploadSession.setStatus(FileUploadSessionStatus.COMPLETED);
        uploadSession.setModifiedId(auth.userId());
        fileObjectRepository.save(file);
        uploadSessionRepository.save(uploadSession);
        return toFileInfo(file);
    }

    @Override
    @Transactional
    public FileInfo abortUploadSession(String uploadSessionNo) {
        CurrentPrincipalContext auth = currentAuth.require("用户未登录");
        FileUploadSessionPO uploadSession = requireUploadSession(uploadSessionNo);
        FileObjectPO file = requireFile(uploadSession.getFileNo());
        requireSameUploader(auth, file);
        requireBusinessApp(file);

        if (uploadSession.getStatus() == FileUploadSessionStatus.COMPLETED) {
            throw new BusinessException("上传已完成，不能取消");
        }
        uploadSession.setStatus(FileUploadSessionStatus.ABORTED);
        uploadSession.setModifiedId(auth.userId());
        file.setStatus(FileStatus.DELETED);
        file.setModifiedId(auth.userId());
        file.setDeleted(1);
        file.setDeletedTime(ForestTime.now());
        uploadSessionRepository.save(uploadSession);
        fileObjectRepository.save(file);
        return toFileInfo(file);
    }

    @Override
    @Transactional(readOnly = true)
    public FileInfo getFile(String fileNo) {
        CurrentPrincipalContext auth = currentAuth.require("用户未登录");
        FileObjectPO file = requireFile(fileNo);
        requireBusinessApp(file);
        requireReadable(auth, file);
        return toFileInfo(file);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DownloadUrlResult> createDownloadUrls(List<String> fileNos) {
        return createAccessUrls(fileNos, AccessUrlDisposition.ATTACHMENT);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DownloadUrlResult> createPreviewUrls(List<String> fileNos) {
        return createAccessUrls(fileNos, AccessUrlDisposition.INLINE);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadUrlResult createDownloadUrl(String fileNo) {
        return createAccessUrl(fileNo, AccessUrlDisposition.ATTACHMENT);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadUrlResult createPreviewUrl(String fileNo) {
        return createAccessUrl(fileNo, AccessUrlDisposition.INLINE);
    }

    private List<DownloadUrlResult> createAccessUrls(List<String> fileNos, AccessUrlDisposition disposition) {
        if (fileNos == null || fileNos.isEmpty()) {
            return List.of();
        }
        return fileNos.stream()
            .filter(fileNo -> fileNo != null && !fileNo.isBlank())
            .distinct()
            .map(fileNo -> createAccessUrl(fileNo, disposition))
            .toList();
    }

    private DownloadUrlResult createAccessUrl(String fileNo, AccessUrlDisposition disposition) {
        CurrentPrincipalContext auth = currentAuth.require("用户未登录");
        FileObjectPO file = requireFile(fileNo);
        requireBusinessApp(file);
        requireReadable(auth, file);
        requireAvailable(file);

        LocalDateTime expiresAt = ForestTime.now().plus(safeDuration(properties.getDownloadUrlTtl(), Duration.ofMinutes(5)));
        ObjectStorageDownloadUrl downloadUrl = objectStorageClient.createDownloadUrl(new ObjectStorageDownloadRequest(
            file.getBucket(),
            file.getObjectKey(),
            contentDisposition(disposition, file.getOriginalName()),
            toInstant(expiresAt)
        ));
        return new DownloadUrlResult(file.getFileNo(), downloadUrl.url(), toLocalDateTime(downloadUrl.expiresAt()));
    }

    @Override
    @Transactional
    public void deleteFile(String fileNo) {
        CurrentPrincipalContext auth = currentAuth.require("用户未登录");
        FileObjectPO file = requireFile(fileNo);
        requireBusinessApp(file);
        if (!accessPolicy.canDelete(auth, file)) {
            throw new BusinessException("无权删除文件");
        }
        if (file.getStatus() == FileStatus.DELETED) {
            return;
        }
        objectStorageClient.deleteObject(file.getBucket(), file.getObjectKey());
        file.setStatus(FileStatus.DELETED);
        file.setModifiedId(auth.userId());
        file.setDeleted(1);
        file.setDeletedTime(ForestTime.now());
        fileObjectRepository.save(file);
    }

    private void validateUploadedObject(FileUploadSessionPO uploadSession, ObjectStorageObjectMetadata metadata) {
        if (metadata.contentLength() != uploadSession.getExpectedSizeBytes()) {
            throw new BusinessException("上传文件大小不匹配");
        }
        String actualContentType = normalizeContentType(metadata.contentType());
        String expectedContentType = normalizeContentType(uploadSession.getExpectedContentType());
        if (!expectedContentType.equals(actualContentType)) {
            throw new BusinessException("上传文件类型不匹配");
        }
    }

    private void requireUploadSessionUsable(FileUploadSessionPO uploadSession, Long operatorId) {
        if (uploadSession.getStatus() != FileUploadSessionStatus.CREATED) {
            throw new BusinessException("上传会话状态不可确认");
        }
        if (uploadSession.getExpiresAt().isBefore(ForestTime.now())) {
            uploadSession.setStatus(FileUploadSessionStatus.EXPIRED);
            uploadSession.setModifiedId(operatorId);
            uploadSessionRepository.save(uploadSession);
            throw new BusinessException("上传会话已过期");
        }
    }

    private void requireReadable(CurrentPrincipalContext auth, FileObjectPO file) {
        if (!accessPolicy.canRead(auth, file)) {
            throw new BusinessException("无权访问文件");
        }
    }

    private void requireAvailable(FileObjectPO file) {
        if (file.getStatus() != FileStatus.AVAILABLE) {
            throw new BusinessException("文件不可用");
        }
    }

    private void requireSameUploader(CurrentPrincipalContext auth, FileObjectPO file) {
        if (!auth.userId().equals(file.getUploaderUserId())) {
            throw new BusinessException("无权操作上传会话");
        }
    }

    private void requireBusinessApp(FileObjectPO file) {
        if (!requireBusinessAppCode().equals(file.getBusinessAppCode())) {
            throw new BusinessException("文件不属于当前应用");
        }
    }

    private FileObjectPO requireFile(String fileNo) {
        return fileObjectRepository.findByFileNoAndDeleted(requireText(fileNo, "fileNo 不能为空"), 0)
            .orElseThrow(() -> new BusinessException("文件不存在"));
    }

    private FileUploadSessionPO requireUploadSession(String uploadSessionNo) {
        return uploadSessionRepository.findByUploadSessionNoAndDeleted(requireText(uploadSessionNo, "uploadSessionNo 不能为空"), 0)
            .orElseThrow(() -> new BusinessException("上传会话不存在"));
    }

    private String nextFileNo() {
        String fileNo;
        do {
            fileNo = numberGenerator.nextFileNo();
        } while (fileObjectRepository.existsByFileNo(fileNo));
        return fileNo;
    }

    private String nextUploadSessionNo() {
        String sessionNo;
        do {
            sessionNo = numberGenerator.nextUploadSessionNo();
        } while (uploadSessionRepository.existsByUploadSessionNo(sessionNo));
        return sessionNo;
    }

    private String requireBusinessAppCode() {
        return requireText(properties.getAppCode(), "文件业务 appCode 未配置");
    }

    private FileInfo toFileInfo(FileObjectPO file) {
        return new FileInfo(
            file.getFileNo(),
            file.getBusinessAppCode(),
            file.getUploadedClientAppCode(),
            file.getOriginalName(),
            file.getContentType(),
            file.getFileCategory(),
            file.getSizeBytes(),
            file.getEtag(),
            file.getStatus(),
            file.getCreatedTime()
        );
    }

    private String normalizeContentType(String value) {
        return requireText(value, "OSS 对象 contentType 不能为空").toLowerCase(Locale.ROOT);
    }

    private String contentDisposition(AccessUrlDisposition disposition, String originalName) {
        String safeName = requireText(originalName, "文件名不能为空").replace("\"", "");
        String type = disposition == AccessUrlDisposition.INLINE ? "inline" : "attachment";
        return type + "; filename=\"" + safeName + "\"";
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private Duration safeDuration(Duration duration, Duration fallback) {
        return duration == null || duration.isNegative() || duration.isZero() ? fallback : duration;
    }

    private Instant toInstant(LocalDateTime value) {
        return value.atZone(ForestTime.ZONE_ID).toInstant();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return LocalDateTime.ofInstant(value, ForestTime.ZONE_ID);
    }

    private enum AccessUrlDisposition {
        ATTACHMENT,
        INLINE
    }
}
