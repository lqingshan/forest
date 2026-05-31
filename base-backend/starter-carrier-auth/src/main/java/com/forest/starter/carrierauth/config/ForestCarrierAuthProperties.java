package com.forest.starter.carrierauth.config;

import com.forest.starter.carrierauth.CarrierAuthProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for native carrier one-click-login authentication.
 */
@ConfigurationProperties(prefix = "forest.carrier-auth")
public class ForestCarrierAuthProperties {
    private CarrierAuthProvider provider = CarrierAuthProvider.DISABLED;
    private String mockPhone = "+8618257147892";
    private Aliyun aliyun = new Aliyun();

    public CarrierAuthProvider getProvider() {
        return provider;
    }

    public void setProvider(CarrierAuthProvider provider) {
        this.provider = provider;
    }

    public String getMockPhone() {
        return mockPhone;
    }

    public void setMockPhone(String mockPhone) {
        this.mockPhone = mockPhone;
    }

    public Aliyun getAliyun() {
        return aliyun;
    }

    public void setAliyun(Aliyun aliyun) {
        this.aliyun = aliyun;
    }

    public static class Aliyun {
        private String endpoint = "dypnsapi.aliyuncs.com";
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
