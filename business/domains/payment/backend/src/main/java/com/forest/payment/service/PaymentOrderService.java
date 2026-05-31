package com.forest.payment.service;

import com.forest.payment.client.service.PaymentClientService;
import com.forest.payment.entity.PaymentOrderPO;

import java.util.Map;

/**
 * 定义支付单核心能力。
 */
public interface PaymentOrderService {
    PaymentClientService.PaymentCreationResult createOrder(Long userId, String bizType, Long bizOrderId);

    PaymentOrderPO getRequiredById(Long paymentOrderId);

    PaymentOrderPO getRequiredById(Long userId, Long paymentOrderId);

    void handleWechatPayNotify(String requestBody, Map<String, String> headers);
}
