package com.forest.starter.redis.config;

import com.forest.starter.json.ForestObjectMappers;
import com.forest.starter.redis.client.ForestRedisClient;
import com.forest.starter.redis.client.ForestRedisJsonClient;
import com.forest.starter.redis.client.ForestRedisLockClient;
import com.forest.starter.redis.client.ForestRedisRateLimiter;
import com.forest.starter.redis.key.RedisKeyFactory;
import com.forest.starter.redis.key.RedisKeyValidator;
import com.forest.starter.redis.ttl.ForestRedisTtlPolicy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Auto-configuration for Forest Redis helper APIs.
 */
@AutoConfiguration
@AutoConfigureAfter(name = "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration")
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "forest.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ForestRedisProperties.class)
public class ForestRedisAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RedisKeyValidator redisKeyValidator() {
        return new RedisKeyValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisKeyFactory redisKeyFactory(ForestRedisProperties properties, RedisKeyValidator validator) {
        return new RedisKeyFactory(properties.getKeyPrefix(), properties.getAppCode(), properties.isValidateKey(), validator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ForestRedisTtlPolicy forestRedisTtlPolicy(ForestRedisProperties properties) {
        return new ForestRedisTtlPolicy(properties);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean
    public ForestRedisClient forestRedisClient(StringRedisTemplate redisTemplate) {
        return new ForestRedisClient(redisTemplate);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean
    public ForestRedisRateLimiter forestRedisRateLimiter(StringRedisTemplate redisTemplate) {
        return new ForestRedisRateLimiter(redisTemplate);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean
    public ForestRedisLockClient forestRedisLockClient(StringRedisTemplate redisTemplate) {
        return new ForestRedisLockClient(redisTemplate);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean
    public ForestRedisJsonClient forestRedisJsonClient(StringRedisTemplate redisTemplate, ObjectProvider<JsonMapper> objectMapper) {
        return new ForestRedisJsonClient(redisTemplate, objectMapper.getIfAvailable(ForestObjectMappers::defaultJsonMapper));
    }
}
