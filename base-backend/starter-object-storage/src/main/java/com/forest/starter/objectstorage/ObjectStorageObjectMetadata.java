package com.forest.starter.objectstorage;

/**
 * 表示对象存储中已存在对象的基础元数据。
 */
public record ObjectStorageObjectMetadata(
    String bucket,
    String objectKey,
    long contentLength,
    String contentType,
    String etag
) {
}
