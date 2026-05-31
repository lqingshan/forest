package com.forest.starter.objectstorage.mock;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.objectstorage.ObjectStorageClient;
import com.forest.starter.objectstorage.ObjectStorageDownloadRequest;
import com.forest.starter.objectstorage.ObjectStorageDownloadUrl;
import com.forest.starter.objectstorage.ObjectStorageObjectMetadata;
import com.forest.starter.objectstorage.ObjectStorageUploadCredential;
import com.forest.starter.objectstorage.ObjectStorageUploadRequest;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory object storage client for tests.
 */
public class MockObjectStorageClient implements ObjectStorageClient {
    private final Map<String, ObjectStorageObjectMetadata> objects = new ConcurrentHashMap<>();

    @Override
    public ObjectStorageUploadCredential createUploadCredential(ObjectStorageUploadRequest request) {
        return new ObjectStorageUploadCredential(
            request.bucket(),
            request.objectKey(),
            "https://mock-oss.local/" + request.bucket(),
            "POST",
            Map.of(
                "key", request.objectKey(),
                "Content-Type", request.contentType()
            ),
            Map.of(),
            request.expiresAt()
        );
    }

    @Override
    public ObjectStorageObjectMetadata headObject(String bucket, String objectKey) {
        ObjectStorageObjectMetadata metadata = objects.get(key(bucket, objectKey));
        if (metadata == null) {
            throw new BusinessException("OSS 对象不存在");
        }
        return metadata;
    }

    @Override
    public ObjectStorageDownloadUrl createDownloadUrl(ObjectStorageDownloadRequest request) {
        return new ObjectStorageDownloadUrl(
            "https://mock-oss.local/download/" + request.bucket() + "/" + request.objectKey(),
            request.expiresAt()
        );
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        objects.remove(key(bucket, objectKey));
    }

    public void putObject(String bucket, String objectKey, long contentLength, String contentType, String etag) {
        objects.put(key(bucket, objectKey), new ObjectStorageObjectMetadata(
            bucket,
            objectKey,
            contentLength,
            contentType,
            etag == null || etag.isBlank() ? "mock-etag-" + Instant.now().toEpochMilli() : etag
        ));
    }

    private String key(String bucket, String objectKey) {
        return bucket + "/" + objectKey;
    }
}
