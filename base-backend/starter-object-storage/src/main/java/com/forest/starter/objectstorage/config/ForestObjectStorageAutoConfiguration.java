package com.forest.starter.objectstorage.config;

import com.forest.starter.objectstorage.ObjectStorageBucketResolver;
import com.forest.starter.objectstorage.ObjectStorageClient;
import com.forest.starter.objectstorage.aliyun.AliyunOssObjectStorageClient;
import com.forest.starter.objectstorage.aliyun.ConfiguredObjectStorageBucketResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Forest object storage abstractions.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "forest.file", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ForestObjectStorageProperties.class)
public class ForestObjectStorageAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ObjectStorageBucketResolver objectStorageBucketResolver(ForestObjectStorageProperties properties) {
        return new ConfiguredObjectStorageBucketResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectStorageClient objectStorageClient(ForestObjectStorageProperties properties) {
        return new AliyunOssObjectStorageClient(properties);
    }
}
