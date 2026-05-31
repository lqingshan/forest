package com.forest.starter.ocr.config;

import com.forest.starter.ocr.OcrClient;
import com.forest.starter.ocr.disabled.DisabledOcrClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Registers the default disabled OCR client.
 */
@AutoConfiguration
public class ForestOcrAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(OcrClient.class)
    public OcrClient ocrClient() {
        return new DisabledOcrClient();
    }
}
