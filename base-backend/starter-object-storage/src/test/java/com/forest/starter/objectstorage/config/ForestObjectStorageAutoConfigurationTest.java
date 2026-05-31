package com.forest.starter.objectstorage.config;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.objectstorage.ObjectStorageBucketResolver;
import com.forest.starter.objectstorage.ObjectStorageClient;
import com.forest.starter.objectstorage.aliyun.AliyunOssObjectStorageClient;
import com.forest.starter.objectstorage.mock.MockObjectStorageClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestObjectStorageAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ForestObjectStorageAutoConfiguration.class));

    @Test
    void createsAliyunOssClientAndBucketResolverByDefault() {
        contextRunner
            .withPropertyValues(
                "forest.file.storage.aliyun-oss.buckets.cxc-commerce=cxc-commerce-file"
            )
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertTrue(context.getBean(ObjectStorageClient.class) instanceof AliyunOssObjectStorageClient);
                assertEquals(
                    "cxc-commerce-file",
                    context.getBean(ObjectStorageBucketResolver.class).requireBucket("cxc-commerce")
                );
            });
    }

    @Test
    void keepsCustomObjectStorageClient() {
        MockObjectStorageClient customClient = new MockObjectStorageClient();
        contextRunner
            .withBean(ObjectStorageClient.class, () -> customClient)
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertSame(customClient, context.getBean(ObjectStorageClient.class));
            });
    }

    @Test
    void bucketResolverFailsWhenAppBucketIsMissing() {
        contextRunner.run(context -> {
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> context.getBean(ObjectStorageBucketResolver.class).requireBucket("trade-leads")
            );
            assertEquals("文件 bucket 未配置：trade-leads", exception.getMessage());
        });
    }
}
