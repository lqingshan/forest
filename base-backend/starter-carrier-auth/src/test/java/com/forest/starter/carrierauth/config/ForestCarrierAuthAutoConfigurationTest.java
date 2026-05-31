package com.forest.starter.carrierauth.config;

import com.forest.starter.carrierauth.CarrierAuthClient;
import com.forest.starter.carrierauth.disabled.DisabledCarrierAuthClient;
import com.forest.starter.carrierauth.mock.MockCarrierAuthClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class ForestCarrierAuthAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ForestCarrierAuthAutoConfiguration.class));

    @Test
    void createsDisabledClientByDefault() {
        contextRunner.run(context ->
            assertInstanceOf(DisabledCarrierAuthClient.class, context.getBean(CarrierAuthClient.class))
        );
    }

    @Test
    void createsMockClientWhenConfigured() {
        contextRunner
            .withPropertyValues("forest.carrier-auth.provider=mock")
            .run(context ->
                assertInstanceOf(MockCarrierAuthClient.class, context.getBean(CarrierAuthClient.class))
            );
    }

    @Test
    void backsOffWhenCustomClientExists() {
        CarrierAuthClient customClient = request -> null;

        contextRunner
            .withBean(CarrierAuthClient.class, () -> customClient)
            .run(context ->
                assertSame(customClient, context.getBean(CarrierAuthClient.class))
            );
    }
}
