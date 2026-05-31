package com.forest.file.config;

import com.forest.file.service.DefaultFileAccessPolicy;
import com.forest.file.service.FileAccessPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件业务域配置入口。
 */
@Configuration
@EnableConfigurationProperties(ForestFileProperties.class)
public class FileDomainConfig {
    @Bean
    @ConditionalOnMissingBean(FileAccessPolicy.class)
    public DefaultFileAccessPolicy defaultFileAccessPolicy() {
        return new DefaultFileAccessPolicy();
    }
}
