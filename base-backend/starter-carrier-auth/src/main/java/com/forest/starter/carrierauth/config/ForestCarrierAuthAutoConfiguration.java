package com.forest.starter.carrierauth.config;

import com.forest.starter.carrierauth.CarrierAuthClient;
import com.forest.starter.carrierauth.aliyun.AliyunCarrierAuthClient;
import com.forest.starter.carrierauth.disabled.DisabledCarrierAuthClient;
import com.forest.starter.carrierauth.mock.MockCarrierAuthClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for native carrier authentication.
 */
@AutoConfiguration
@EnableConfigurationProperties(ForestCarrierAuthProperties.class)
public class ForestCarrierAuthAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public CarrierAuthClient carrierAuthClient(ForestCarrierAuthProperties properties) {
        return switch (properties.getProvider()) {
            case ALIYUN -> new AliyunCarrierAuthClient(properties);
            case MOCK -> new MockCarrierAuthClient(properties);
            case DISABLED -> new DisabledCarrierAuthClient();
        };
    }
}
