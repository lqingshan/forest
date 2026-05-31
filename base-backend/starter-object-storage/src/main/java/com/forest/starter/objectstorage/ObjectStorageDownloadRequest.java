package com.forest.starter.objectstorage;

import java.time.Instant;

/**
 * 表示创建私有下载 URL 所需的对象存储参数。
 */
public record ObjectStorageDownloadRequest(
    String bucket,
    String objectKey,
    String contentDisposition,
    Instant expiresAt
) {
}
