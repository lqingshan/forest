package com.forest.starter.wechat.config;

import com.forest.starter.json.ForestObjectMappers;
import com.forest.starter.wechat.miniapp.DefaultWechatMiniappClient;
import com.forest.starter.wechat.miniapp.WechatMiniappClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Auto-configuration for Forest WeChat helper APIs.
 */
@AutoConfiguration
@EnableConfigurationProperties(ForestWechatProperties.class)
public class ForestWechatAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(WechatMiniappClient.class)
    public WechatMiniappClient wechatMiniappClient(
        ForestWechatProperties properties,
        ObjectProvider<JsonMapper> objectMapper
    ) {
        JsonMapper wechatMapper = ForestObjectMappers.copyForHttpClient(objectMapper.getIfAvailable(ForestObjectMappers::defaultJsonMapper));
        return new DefaultWechatMiniappClient(properties, new RestTemplate(), wechatMapper);
    }
}
