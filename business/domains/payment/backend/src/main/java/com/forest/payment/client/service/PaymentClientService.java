package com.forest.payment.client.service;

import com.forest.payment.channel.PaymentChannelGateway;
import com.forest.payment.entity.PaymentOrderPO;

/**
 * 定义用户端支付能力。
 */
public interface PaymentClientService {
    PaymentCreationResult createOrder(Long userId, String bizType, Long bizOrderId);

    PaymentOrderPO getOrder(Long userId, Long paymentOrderId);

    record PaymentCreationResult(
        PaymentOrderPO paymentOrder,
        PaymentChannelGateway.WechatMiniappPaymentResult paymentResult
    ) {
    }
}
