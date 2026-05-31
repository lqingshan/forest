package com.forest.starter.objectstorage;

/**
 * 根据业务应用编码解析对象存储 bucket。
 */
public interface ObjectStorageBucketResolver {
    String requireBucket(String appCode);
}
