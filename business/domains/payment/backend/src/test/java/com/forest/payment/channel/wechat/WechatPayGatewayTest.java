package com.forest.payment.channel.wechat;

import com.forest.payment.channel.PaymentChannelGateway;
import com.forest.payment.config.WechatPayProperties;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.json.ForestObjectMappers;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WechatPayGatewayTest {
    @Test
    void createWechatMiniappPaymentOrderReturnsMockMiniappParams() {
        WechatPayGateway gateway = gateway(mockProperties());

        PaymentChannelGateway.WechatMiniappPaymentResult result = gateway.createWechatMiniappPaymentOrder(new PaymentChannelGateway.WechatMiniappPaymentRequest(
            "积分充值",
            "OUT-001",
            9900,
            "mock-openid",
            "https://api.forest.example/api/open/wechat/pay/notify"
        ));

        assertTrue(result.prepayId().startsWith("mock-prepay-"));
        assertTrue(result.packageValue().startsWith("prepay_id=mock-prepay-"));
        assertEquals("RSA", result.signType());
        assertEquals("mock-pay-sign", result.paySign());
        assertNotNull(result.timeStamp());
        assertNotNull(result.nonceStr());
    }

    @Test
    void parseAndVerifyNotifyReturnsMockPaymentFact() {
        WechatPayGateway gateway = gateway(mockProperties());

        PaymentChannelGateway.WechatPayNotifyResult result = gateway.parseAndVerifyNotify(
            """
            {
              "outTradeNo": "OUT-001",
              "transactionId": "mock-tx-001",
              "amountCents": 9900
            }
            """,
            Map.of()
        );

        assertEquals("OUT-001", result.outTradeNo());
        assertEquals("mock-tx-001", result.transactionId());
        assertEquals(9900, result.amountCents());
        assertNotNull(result.paidTime());
    }

    @Test
    void parsePaidTimeConvertsUtcOffsetToAsiaShanghai() {
        WechatPayGateway gateway = gateway(mockProperties());

        assertEquals(
            LocalDateTime.of(2026, 4, 29, 20, 0),
            gateway.parsePaidTime("2026-04-29T12:00:00Z")
        );
    }

    @Test
    void parsePaidTimeKeepsAsiaShanghaiOffsetLocalTime() {
        WechatPayGateway gateway = gateway(mockProperties());

        assertEquals(
            LocalDateTime.of(2026, 4, 29, 20, 0),
            gateway.parsePaidTime("2026-04-29T20:00:00+08:00")
        );
    }

    @Test
    void realModeRequiresWechatPayEnabled() {
        WechatPayProperties properties = mockProperties();
        properties.setMockEnabled(false);
        properties.setEnabled(false);

        BusinessException exception = assertThrows(BusinessException.class, () ->
            gateway(properties).createWechatMiniappPaymentOrder(new PaymentChannelGateway.WechatMiniappPaymentRequest(
                "积分充值",
                "OUT-001",
                9900,
                "openid",
                "https://api.forest.example/api/open/wechat/pay/notify"
            )));

        assertEquals("微信支付未启用", exception.getMessage());
    }

    @Test
    void realModeRejectsMissingRequiredConfigBeforeCallingWechat() {
        WechatPayProperties properties = new WechatPayProperties();
        properties.setMockEnabled(false);
        properties.setEnabled(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
            gateway(properties).createWechatMiniappPaymentOrder(new PaymentChannelGateway.WechatMiniappPaymentRequest(
                "积分充值",
                "OUT-001",
                9900,
                "openid",
                "https://api.forest.example/api/open/wechat/pay/notify"
            )));

        assertTrue(exception.getMessage().contains("微信支付配置不完整"));
        assertTrue(exception.getMessage().contains("wechat.pay.mch-id"));
        assertTrue(exception.getMessage().contains("wechat.pay.private-key-path"));
        assertTrue(exception.getMessage().contains("wechat.pay.public-key-id"));
        assertTrue(exception.getMessage().contains("wechat.pay.public-key-path"));
    }

    private WechatPayProperties mockProperties() {
        WechatPayProperties properties = new WechatPayProperties();
        properties.setMockEnabled(true);
        properties.setEnabled(false);
        properties.setTimeoutSeconds(10);
        return properties;
    }

    private WechatPayGateway gateway(WechatPayProperties properties) {
        return new WechatPayGateway(properties, ForestObjectMappers.defaultJsonMapper());
    }
}
