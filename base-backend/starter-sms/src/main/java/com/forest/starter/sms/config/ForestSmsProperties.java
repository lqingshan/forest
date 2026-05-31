package com.forest.starter.sms.config;

import com.forest.starter.sms.SmsProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for SMS provider and SMS content logging conventions.
 */
@ConfigurationProperties(prefix = "forest.sms")
public class ForestSmsProperties {
    private SmsProvider provider = SmsProvider.DISABLED;
    private String defaultSignName = "";
    private String defaultTemplateCode = "";
    private String verificationTemplateCode = "";
    private String verificationContentTemplate = "您的验证码为 {code}";
    private Log log = new Log();
    private Aliyun aliyun = new Aliyun();

    public SmsProvider getProvider() {
        return provider;
    }

    public void setProvider(SmsProvider provider) {
        this.provider = provider;
    }

    public String getDefaultSignName() {
        return defaultSignName;
    }

    public void setDefaultSignName(String defaultSignName) {
        this.defaultSignName = defaultSignName;
    }

    public String getDefaultTemplateCode() {
        return defaultTemplateCode;
    }

    public void setDefaultTemplateCode(String defaultTemplateCode) {
        this.defaultTemplateCode = defaultTemplateCode;
    }

    public String getVerificationTemplateCode() {
        return verificationTemplateCode;
    }

    public void setVerificationTemplateCode(String verificationTemplateCode) {
        this.verificationTemplateCode = verificationTemplateCode;
    }

    public String getVerificationContentTemplate() {
        return verificationContentTemplate;
    }

    public void setVerificationContentTemplate(String verificationContentTemplate) {
        this.verificationContentTemplate = verificationContentTemplate;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Aliyun getAliyun() {
        return aliyun;
    }

    public void setAliyun(Aliyun aliyun) {
        this.aliyun = aliyun;
    }

    public enum ContentMode {
        NONE,
        MASKED,
        MASKED_PARTIAL,
        PLAIN
    }

    public static class Log {
        private ContentMode contentMode = ContentMode.MASKED_PARTIAL;
        private int maskVisibleTail = 2;

        public ContentMode getContentMode() {
            return contentMode;
        }

        public void setContentMode(ContentMode contentMode) {
            this.contentMode = contentMode;
        }

        public int getMaskVisibleTail() {
            return maskVisibleTail;
        }

        public void setMaskVisibleTail(int maskVisibleTail) {
            this.maskVisibleTail = maskVisibleTail;
        }
    }

    public static class Aliyun {
        private String endpoint = "dysmsapi.aliyuncs.com";
        private String accessKeyId = "";
        private String accessKeySecret = "";

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
    }
}
