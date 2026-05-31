package com.forest.starter.objectstorage;

import java.time.Instant;

/**
 * 表示短期有效的私有下载地址。
 */
public record ObjectStorageDownloadUrl(
    String url,
    Instant expiresAt
) {
}
