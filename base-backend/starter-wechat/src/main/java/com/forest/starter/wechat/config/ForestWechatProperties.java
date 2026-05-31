package com.forest.starter.wechat.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for Forest WeChat integrations.
 */
@ConfigurationProperties(prefix = "forest.wechat")
public class ForestWechatProperties {
    private Map<String, Miniapp> miniapps = new LinkedHashMap<>();

    public Map<String, Miniapp> getMiniapps() {
        return miniapps;
    }

    public void setMiniapps(Map<String, Miniapp> miniapps) {
        this.miniapps = miniapps == null ? new LinkedHashMap<>() : miniapps;
    }

    public static class Miniapp {
        private String appid;
        private String secret;
        private boolean mockEnabled;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public boolean isMockEnabled() {
            return mockEnabled;
        }

        public void setMockEnabled(boolean mockEnabled) {
            this.mockEnabled = mockEnabled;
        }
    }
}
