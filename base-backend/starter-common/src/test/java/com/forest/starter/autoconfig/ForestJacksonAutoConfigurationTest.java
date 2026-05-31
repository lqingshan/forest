package com.forest.starter.autoconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestJacksonAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, ForestJacksonAutoConfiguration.class));

    @Test
    void customizesBootJsonMapperWithJavaTimeIsoFormat() {
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());
            JsonMapper objectMapper = context.getBean(JsonMapper.class);

            String json = objectMapper.writeValueAsString(new TimePayload(LocalDateTime.of(2026, 5, 22, 10, 30, 45)));

            assertEquals("{\"issuedAt\":\"2026-05-22T10:30:45\"}", json);
            assertTrue(context.containsBean("forestJsonMapperBuilderCustomizer"));
        });
    }

    @Test
    void keepsCustomJsonMapper() {
        JsonMapper customMapper = JsonMapper.builder().build();

        contextRunner
            .withBean(JsonMapper.class, () -> customMapper)
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertSame(customMapper, context.getBean(JsonMapper.class));
            });
    }

    @Test
    void keepsCustomJsonMapperBuilder() {
        JsonMapper.Builder customBuilder = JsonMapper.builder();

        contextRunner
            .withBean(JsonMapper.Builder.class, () -> customBuilder)
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertSame(customBuilder, context.getBean(JsonMapper.Builder.class));
            });
    }

    record TimePayload(LocalDateTime issuedAt) {
    }
}
