package com.forest.verification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables verification domain configuration properties.
 */
@Configuration
@EnableConfigurationProperties(ForestVerificationProperties.class)
public class VerificationDomainConfig {
}
