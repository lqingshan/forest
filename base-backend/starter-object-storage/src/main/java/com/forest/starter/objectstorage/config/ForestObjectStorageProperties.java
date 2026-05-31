package com.forest.starter.objectstorage.config;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Object storage configuration shared by Forest applications.
 */
@ConfigurationProperties(prefix = "forest.file")
public class ForestObjectStorageProperties {
    private boolean enabled = true;
    private String env = "local";
    private Storage storage = new Storage();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage == null ? new Storage() : storage;
    }

    public static class Storage {
        private String provider = "aliyun-oss";
        private Duration uploadCredentialTtl = Duration.ofMinutes(15);
        private Duration downloadUrlTtl = Duration.ofMinutes(5);
        private AliyunOss aliyunOss = new AliyunOss();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public Duration getUploadCredentialTtl() {
            return uploadCredentialTtl;
        }

        public void setUploadCredentialTtl(Duration uploadCredentialTtl) {
            this.uploadCredentialTtl = uploadCredentialTtl;
        }

        public Duration getDownloadUrlTtl() {
            return downloadUrlTtl;
        }

        public void setDownloadUrlTtl(Duration downloadUrlTtl) {
            this.downloadUrlTtl = downloadUrlTtl;
        }

        public AliyunOss getAliyunOss() {
            return aliyunOss;
        }

        public void setAliyunOss(AliyunOss aliyunOss) {
            this.aliyunOss = aliyunOss == null ? new AliyunOss() : aliyunOss;
        }
    }

    public static class AliyunOss {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private Map<String, String> buckets = new LinkedHashMap<>();

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public Map<String, String> getBuckets() {
            return buckets;
        }

        public void setBuckets(Map<String, String> buckets) {
            this.buckets = buckets == null ? new LinkedHashMap<>() : buckets;
        }
    }
}
