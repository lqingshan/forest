package com.forest.cxccommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 启动 CXC Commerce 应用。
 */
@SpringBootApplication(scanBasePackages = "com.forest")
@EntityScan(basePackages = "com.forest")
@EnableJpaRepositories(basePackages = "com.forest")
public class CxcCommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CxcCommerceApplication.class, args);
    }
}
