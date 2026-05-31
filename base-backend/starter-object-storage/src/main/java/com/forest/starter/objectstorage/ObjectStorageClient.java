package com.forest.starter.objectstorage;

/**
 * 对象存储技术能力入口。
 *
 * <p>业务模块只表达上传、下载、校验和删除意图，不直接依赖 OSS SDK。</p>
 */
public interface ObjectStorageClient {
    ObjectStorageUploadCredential createUploadCredential(ObjectStorageUploadRequest request);

    ObjectStorageObjectMetadata headObject(String bucket, String objectKey);

    ObjectStorageDownloadUrl createDownloadUrl(ObjectStorageDownloadRequest request);

    void deleteObject(String bucket, String objectKey);
}
