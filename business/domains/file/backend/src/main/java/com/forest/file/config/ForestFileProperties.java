package com.forest.file.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件业务域配置。
 */
@ConfigurationProperties(prefix = "forest.file")
public class ForestFileProperties {
    private String appCode;
    private String env = "local";
    private Duration uploadSessionTtl = Duration.ofMinutes(15);
    private Duration downloadUrlTtl = Duration.ofMinutes(5);
    private Limits limits = new Limits();

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Duration getUploadSessionTtl() {
        return uploadSessionTtl;
    }

    public void setUploadSessionTtl(Duration uploadSessionTtl) {
        this.uploadSessionTtl = uploadSessionTtl;
    }

    public Duration getDownloadUrlTtl() {
        return downloadUrlTtl;
    }

    public void setDownloadUrlTtl(Duration downloadUrlTtl) {
        this.downloadUrlTtl = downloadUrlTtl;
    }

    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = limits == null ? new Limits() : limits;
    }

    public static class Limits {
        private long imageMaxSizeBytes = 10L * 1024 * 1024;
        private long documentMaxSizeBytes = 50L * 1024 * 1024;
        private long videoMaxSizeBytes = 50L * 1024 * 1024;
        private long audioMaxSizeBytes = 50L * 1024 * 1024;

        public long getImageMaxSizeBytes() {
            return imageMaxSizeBytes;
        }

        public void setImageMaxSizeBytes(long imageMaxSizeBytes) {
            this.imageMaxSizeBytes = imageMaxSizeBytes;
        }

        public long getDocumentMaxSizeBytes() {
            return documentMaxSizeBytes;
        }

        public void setDocumentMaxSizeBytes(long documentMaxSizeBytes) {
            this.documentMaxSizeBytes = documentMaxSizeBytes;
        }

        public long getVideoMaxSizeBytes() {
            return videoMaxSizeBytes;
        }

        public void setVideoMaxSizeBytes(long videoMaxSizeBytes) {
            this.videoMaxSizeBytes = videoMaxSizeBytes;
        }

        public long getAudioMaxSizeBytes() {
            return audioMaxSizeBytes;
        }

        public void setAudioMaxSizeBytes(long audioMaxSizeBytes) {
            this.audioMaxSizeBytes = audioMaxSizeBytes;
        }
    }
}
