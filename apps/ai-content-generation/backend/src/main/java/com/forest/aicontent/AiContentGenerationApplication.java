package com.forest.aicontent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI 内容生成应用空壳启动类。
 */
@SpringBootApplication(scanBasePackages = "com.forest")
public class AiContentGenerationApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiContentGenerationApplication.class, args);
    }
}
