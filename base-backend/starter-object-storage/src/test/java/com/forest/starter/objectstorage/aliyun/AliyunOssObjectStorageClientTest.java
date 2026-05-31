package com.forest.starter.objectstorage.aliyun;

import com.forest.starter.objectstorage.ObjectStorageUploadCredential;
import com.forest.starter.objectstorage.ObjectStorageUploadRequest;
import com.forest.starter.objectstorage.config.ForestObjectStorageProperties;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AliyunOssObjectStorageClientTest {
    @Test
    void createUploadCredentialUsesBase64EncodedPostPolicy() {
        AliyunOssObjectStorageClient client = new AliyunOssObjectStorageClient(properties());

        ObjectStorageUploadCredential credential = client.createUploadCredential(new ObjectStorageUploadRequest(
            "trade-leads",
            "local/document/2026/05/11/FILE001/test.txt",
            "text/plain",
            128,
            Instant.now().plusSeconds(600)
        ));

        String encodedPolicy = credential.formFields().get("policy");
        assertNotNull(encodedPolicy);
        assertFalse(encodedPolicy.startsWith("{"));
        String decodedPolicy = new String(Base64.getDecoder().decode(encodedPolicy), StandardCharsets.UTF_8);
        assertTrue(decodedPolicy.contains("\"expiration\""));
        assertTrue(decodedPolicy.contains("\"conditions\""));
        assertTrue(decodedPolicy.contains("content-length-range"));
        assertEquals("POST", credential.method());
        assertEquals("https://trade-leads.oss-cn-hangzhou.aliyuncs.com", credential.uploadUrl());
        assertNotNull(credential.formFields().get("signature"));

        client.shutdown();
    }

    private ForestObjectStorageProperties properties() {
        ForestObjectStorageProperties properties = new ForestObjectStorageProperties();
        ForestObjectStorageProperties.AliyunOss aliyun = properties.getStorage().getAliyunOss();
        aliyun.setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");
        aliyun.setAccessKeyId("test-access-key-id");
        aliyun.setAccessKeySecret("test-access-key-secret");
        return properties;
    }
}
