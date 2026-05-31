package com.forest.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 启动 Attendance 考勤系统应用。
 */
@SpringBootApplication(scanBasePackages = "com.forest")
@EntityScan(basePackages = "com.forest")
@EnableJpaRepositories(basePackages = "com.forest")
public class AttendanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AttendanceApplication.class, args);
    }
}
