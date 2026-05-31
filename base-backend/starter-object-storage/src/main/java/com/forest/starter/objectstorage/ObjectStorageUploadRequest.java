package com.forest.starter.objectstorage;

import java.time.Instant;

/**
 * 表示创建直传凭证所需的对象存储参数。
 */
public record ObjectStorageUploadRequest(
    String bucket,
    String objectKey,
    String contentType,
    long maxSizeBytes,
    Instant expiresAt
) {
}
