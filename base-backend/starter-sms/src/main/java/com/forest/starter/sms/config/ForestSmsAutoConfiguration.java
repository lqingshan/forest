package com.forest.starter.sms.config;

import com.forest.starter.json.ForestObjectMappers;
import com.forest.starter.sms.SmsSender;
import com.forest.starter.sms.aliyun.AliyunSmsSender;
import com.forest.starter.sms.disabled.DisabledSmsSender;
import com.forest.starter.sms.mock.MockSmsSender;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Auto-configuration for Forest SMS sender abstraction.
 */
@AutoConfiguration
@EnableConfigurationProperties(ForestSmsProperties.class)
public class ForestSmsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SmsSender smsSender(ForestSmsProperties properties, ObjectProvider<JsonMapper> objectMapper) {
        return switch (properties.getProvider()) {
            case ALIYUN -> new AliyunSmsSender(properties, objectMapper.getIfAvailable(ForestObjectMappers::defaultJsonMapper));
            case MOCK -> new MockSmsSender();
            case DISABLED -> new DisabledSmsSender();
        };
    }
}
