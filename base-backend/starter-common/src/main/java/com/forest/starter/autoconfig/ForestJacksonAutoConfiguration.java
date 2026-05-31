package com.forest.starter.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Project-wide Jackson defaults for the Jackson 3 JsonMapper used by Forest modules.
 */
@AutoConfiguration
//@AutoConfigureBefore(JacksonAutoConfiguration.class)
@ConditionalOnClass({JsonMapper.class, JsonMapperBuilderCustomizer.class})
public class ForestJacksonAutoConfiguration {
    @Bean
    public JsonMapperBuilderCustomizer forestJsonMapperBuilderCustomizer() {
        return builder -> builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
