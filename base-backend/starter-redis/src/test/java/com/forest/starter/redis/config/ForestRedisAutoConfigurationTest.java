package com.forest.starter.redis.config;

import java.time.Duration;

import com.forest.starter.redis.client.ForestRedisClient;
import com.forest.starter.redis.client.ForestRedisJsonClient;
import com.forest.starter.redis.key.RedisKeyFactory;
import com.forest.starter.redis.ttl.ForestRedisTtlPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ForestRedisAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ForestRedisAutoConfiguration.class))
        .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
        .withBean(JsonMapper.class, () -> JsonMapper.builder().build());

    @Test
    void createsStarterBeansWhenConfigured() {
        contextRunner
            .withPropertyValues("forest.redis.app-code=cxc-commerce")
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertTrue(context.containsBean("redisKeyFactory"));
                assertTrue(context.containsBean("forestRedisClient"));
                assertTrue(context.containsBean("forestRedisJsonClient"));
                assertEquals(
                    "forest:cxc-commerce:auth:sms:13800138000",
                    context.getBean(RedisKeyFactory.class).authSms("13800138000").value()
                );
            });
    }

    @Test
    void doesNotCreateStarterBeansWhenDisabled() {
        contextRunner
            .withPropertyValues("forest.redis.enabled=false")
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertTrue(!context.containsBean("redisKeyFactory"));
                assertTrue(!context.containsBean("forestRedisClient"));
            });
    }

    @Test
    void failsFastWhenAppCodeIsMissing() {
        contextRunner.run(context -> assertNotNull(context.getStartupFailure()));
    }

    @Test
    void bindsCustomPrefixAndTtl() {
        contextRunner
            .withPropertyValues(
                "forest.redis.key-prefix=custom",
                "forest.redis.app-code=trade-leads",
                "forest.redis.ttl.sms-code=10m"
            )
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertEquals("custom:trade-leads:auth:sms:13800138000", context.getBean(RedisKeyFactory.class).authSms("13800138000").value());
                assertEquals(Duration.ofMinutes(10), context.getBean(ForestRedisTtlPolicy.class).smsCode());
                assertNotNull(context.getBean(ForestRedisClient.class));
                assertNotNull(context.getBean(ForestRedisJsonClient.class));
            });
    }
}
