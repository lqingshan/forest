package com.forest.starter.objectstorage.aliyun;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.objectstorage.ObjectStorageBucketResolver;
import com.forest.starter.objectstorage.config.ForestObjectStorageProperties;

/**
 * Resolves object storage buckets from forest.file.storage.aliyun-oss.buckets.
 */
public class ConfiguredObjectStorageBucketResolver implements ObjectStorageBucketResolver {
    private final ForestObjectStorageProperties properties;

    public ConfiguredObjectStorageBucketResolver(ForestObjectStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String requireBucket(String appCode) {
        String safeAppCode = requireText(appCode, "文件业务 appCode 不能为空");
        String bucket = properties.getStorage().getAliyunOss().getBuckets().get(safeAppCode);
        return requireText(bucket, "文件 bucket 未配置：" + safeAppCode);
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }
}
