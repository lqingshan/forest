package com.forest.starter.objectstorage.aliyun;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PolicyConditions;
import com.aliyun.oss.model.ResponseHeaderOverrides;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.objectstorage.ObjectStorageClient;
import com.forest.starter.objectstorage.ObjectStorageDownloadRequest;
import com.forest.starter.objectstorage.ObjectStorageDownloadUrl;
import com.forest.starter.objectstorage.ObjectStorageObjectMetadata;
import com.forest.starter.objectstorage.ObjectStorageUploadCredential;
import com.forest.starter.objectstorage.ObjectStorageUploadRequest;
import com.forest.starter.objectstorage.config.ForestObjectStorageProperties;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Aliyun OSS implementation of the object storage abstraction.
 */
public class AliyunOssObjectStorageClient implements ObjectStorageClient {
    private static final String PROVIDER = "aliyun-oss";

    private final ForestObjectStorageProperties properties;
    private final AtomicReference<OSS> ossClient = new AtomicReference<>();

    public AliyunOssObjectStorageClient(ForestObjectStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public ObjectStorageUploadCredential createUploadCredential(ObjectStorageUploadRequest request) {
        requireProvider();
        requireUploadRequest(request);

        Date expiration = Date.from(request.expiresAt());
        PolicyConditions conditions = new PolicyConditions();
        conditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 1, request.maxSizeBytes());
        conditions.addConditionItem(PolicyConditions.COND_KEY, request.objectKey());
        conditions.addConditionItem(PolicyConditions.COND_CONTENT_TYPE, request.contentType());

        String policy = client().generatePostPolicy(expiration, conditions);
        String encodedPolicy = Base64.getEncoder().encodeToString(policy.getBytes(StandardCharsets.UTF_8));
        String signature = client().calculatePostSignature(policy);

        Map<String, String> formFields = new LinkedHashMap<>();
        formFields.put("key", request.objectKey());
        formFields.put("policy", encodedPolicy);
        formFields.put("OSSAccessKeyId", requireAliyun().getAccessKeyId().trim());
        formFields.put("signature", signature);
        formFields.put("success_action_status", "200");
        formFields.put("Content-Type", request.contentType());

        return new ObjectStorageUploadCredential(
            request.bucket(),
            request.objectKey(),
            uploadUrl(request.bucket()),
            "POST",
            formFields,
            Map.of(),
            request.expiresAt()
        );
    }

    @Override
    public ObjectStorageObjectMetadata headObject(String bucket, String objectKey) {
        requireProvider();
        requireText(bucket, "bucket 不能为空");
        requireText(objectKey, "objectKey 不能为空");
        try {
            ObjectMetadata metadata = client().getObjectMetadata(bucket.trim(), objectKey.trim());
            return new ObjectStorageObjectMetadata(
                bucket.trim(),
                objectKey.trim(),
                metadata.getContentLength(),
                metadata.getContentType(),
                metadata.getETag()
            );
        } catch (OSSException | ClientException ex) {
            throw new BusinessException("OSS 对象校验失败", ex);
        }
    }

    @Override
    public ObjectStorageDownloadUrl createDownloadUrl(ObjectStorageDownloadRequest request) {
        requireProvider();
        requireDownloadRequest(request);

        GeneratePresignedUrlRequest signedRequest = new GeneratePresignedUrlRequest(
            request.bucket(),
            request.objectKey(),
            HttpMethod.GET
        );
        signedRequest.setExpiration(Date.from(request.expiresAt()));
        if (request.contentDisposition() != null && !request.contentDisposition().isBlank()) {
            ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
            responseHeaders.setContentDisposition(request.contentDisposition());
            signedRequest.setResponseHeaders(responseHeaders);
        }

        URL url = client().generatePresignedUrl(signedRequest);
        return new ObjectStorageDownloadUrl(url.toString(), request.expiresAt());
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        requireProvider();
        requireText(bucket, "bucket 不能为空");
        requireText(objectKey, "objectKey 不能为空");
        try {
            client().deleteObject(bucket.trim(), objectKey.trim());
        } catch (OSSException | ClientException ex) {
            throw new BusinessException("OSS 对象删除失败", ex);
        }
    }

    public void shutdown() {
        OSS client = ossClient.getAndSet(null);
        if (client != null) {
            client.shutdown();
        }
    }

    private OSS client() {
        OSS existing = ossClient.get();
        if (existing != null) {
            return existing;
        }
        OSS created = createOssClient(properties);
        if (ossClient.compareAndSet(null, created)) {
            return created;
        }
        created.shutdown();
        return ossClient.get();
    }

    public static OSS createOssClient(ForestObjectStorageProperties properties) {
        ForestObjectStorageProperties.AliyunOss aliyun = properties.getStorage().getAliyunOss();
        String endpoint = requireText(aliyun.getEndpoint(), "OSS endpoint 未配置");
        String accessKeyId = requireText(aliyun.getAccessKeyId(), "OSS accessKeyId 未配置");
        String accessKeySecret = requireText(aliyun.getAccessKeySecret(), "OSS accessKeySecret 未配置");
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    private String uploadUrl(String bucket) {
        String endpoint = requireAliyun().getEndpoint().trim();
        String normalized = endpoint.startsWith("http://") || endpoint.startsWith("https://")
            ? endpoint
            : "https://" + endpoint;
        String withoutScheme = normalized.replaceFirst("^https?://", "");
        return normalized.startsWith("https://")
            ? "https://" + bucket.trim() + "." + withoutScheme
            : "http://" + bucket.trim() + "." + withoutScheme;
    }

    private void requireProvider() {
        String provider = properties.getStorage().getProvider();
        if (provider != null && !PROVIDER.equals(provider.trim())) {
            throw new BusinessException("对象存储 provider 不支持：" + provider);
        }
    }

    private ForestObjectStorageProperties.AliyunOss requireAliyun() {
        return properties.getStorage().getAliyunOss();
    }

    private void requireUploadRequest(ObjectStorageUploadRequest request) {
        if (request == null) {
            throw new BusinessException("上传凭证请求不能为空");
        }
        requireText(request.bucket(), "bucket 不能为空");
        requireText(request.objectKey(), "objectKey 不能为空");
        requireText(request.contentType(), "contentType 不能为空");
        if (request.maxSizeBytes() <= 0) {
            throw new BusinessException("maxSizeBytes 必须大于 0");
        }
        requireFuture(request.expiresAt(), "上传凭证过期时间无效");
    }

    private void requireDownloadRequest(ObjectStorageDownloadRequest request) {
        if (request == null) {
            throw new BusinessException("下载凭证请求不能为空");
        }
        requireText(request.bucket(), "bucket 不能为空");
        requireText(request.objectKey(), "objectKey 不能为空");
        requireFuture(request.expiresAt(), "下载凭证过期时间无效");
    }

    private static void requireFuture(Instant value, String message) {
        if (value == null || !value.isAfter(Instant.now())) {
            throw new BusinessException(message);
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }
}
