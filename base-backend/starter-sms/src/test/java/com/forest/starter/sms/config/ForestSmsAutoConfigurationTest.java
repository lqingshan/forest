package com.forest.starter.sms.config;

import com.forest.starter.sms.SmsSender;
import com.forest.starter.sms.disabled.DisabledSmsSender;
import com.forest.starter.sms.mock.MockSmsSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class ForestSmsAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ForestSmsAutoConfiguration.class));

    @Test
    void createsDisabledSenderByDefault() {
        contextRunner.run(context ->
            assertInstanceOf(DisabledSmsSender.class, context.getBean(SmsSender.class))
        );
    }

    @Test
    void createsMockSenderWhenConfigured() {
        contextRunner
            .withPropertyValues("forest.sms.provider=mock")
            .run(context ->
                assertInstanceOf(MockSmsSender.class, context.getBean(SmsSender.class))
            );
    }

    @Test
    void backsOffWhenCustomSenderExists() {
        SmsSender customSender = request -> null;

        contextRunner
            .withBean(SmsSender.class, () -> customSender)
            .run(context ->
                assertSame(customSender, context.getBean(SmsSender.class))
            );
    }
}
