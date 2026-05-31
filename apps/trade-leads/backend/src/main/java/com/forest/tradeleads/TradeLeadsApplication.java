package com.forest.tradeleads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 启动外贸线索应用。
 */
@SpringBootApplication(scanBasePackages = "com.forest")
@EntityScan(basePackages = "com.forest")
@EnableJpaRepositories(basePackages = "com.forest")
public class TradeLeadsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeLeadsApplication.class, args);
    }
}
