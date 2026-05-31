package com.forest.starter.wechat.config;

import com.forest.starter.wechat.miniapp.DefaultWechatMiniappClient;
import com.forest.starter.wechat.miniapp.WechatCodeSession;
import com.forest.starter.wechat.miniapp.WechatMiniappClient;
import com.forest.starter.wechat.miniapp.WechatPhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ForestWechatAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ForestWechatAutoConfiguration.class));

    @Test
    void createsWechatMiniappClient() {
        contextRunner
            .withPropertyValues(
                "forest.wechat.miniapps.buyer-miniapp.appid=wx-buyer",
                "forest.wechat.miniapps.buyer-miniapp.secret=buyer-secret"
            )
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertFalse(context.containsBean("wechatObjectMapper"));
                assertTrue(context.containsBean("wechatMiniappClient"));
                assertTrue(context.getBean(WechatMiniappClient.class) instanceof DefaultWechatMiniappClient);
            });
    }

    @Test
    void keepsCustomWechatMiniappClient() {
        WechatMiniappClient customClient = new WechatMiniappClient() {
            @Override
            public WechatCodeSession codeToSession(String appCode, String code) {
                return new WechatCodeSession("custom-openid", null, null);
            }

            @Override
            public WechatPhoneNumber getPhoneNumber(String appCode, String phoneCode) {
                return new WechatPhoneNumber("13800138000", "13800138000", "86");
            }
        };

        contextRunner
            .withBean(WechatMiniappClient.class, () -> customClient)
            .withPropertyValues(
                "forest.wechat.miniapps.buyer-miniapp.appid=wx-buyer",
                "forest.wechat.miniapps.buyer-miniapp.secret=buyer-secret"
            )
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertSame(customClient, context.getBean(WechatMiniappClient.class));
                assertEquals("custom-openid", context.getBean(WechatMiniappClient.class)
                    .codeToSession("buyer-miniapp", "code")
                    .openId());
            });
    }
}
