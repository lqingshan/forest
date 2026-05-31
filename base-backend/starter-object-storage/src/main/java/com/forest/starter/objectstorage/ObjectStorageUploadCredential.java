package com.forest.starter.objectstorage;

import java.time.Instant;
import java.util.Map;

/**
 * 表示前端直传对象存储所需的临时凭证。
 */
public record ObjectStorageUploadCredential(
    String bucket,
    String objectKey,
    String uploadUrl,
    String method,
    Map<String, String> formFields,
    Map<String, String> headers,
    Instant expiresAt
) {
}
