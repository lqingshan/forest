package com.forest.payment.channel;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 定义支付渠道接入能力。
 *
 * <p>payment 域通过该接口隔离微信支付等外部渠道，业务服务只处理统一的支付事实。</p>
 */
public interface PaymentChannelGateway {
    /**
     * 创建微信小程序支付预支付订单，返回可直接交给 wx.requestPayment 的参数。
     */
    WechatMiniappPaymentResult createWechatMiniappPaymentOrder(WechatMiniappPaymentRequest request);

    /**
     * 解析并校验微信支付回调，成功后返回已验签的支付事实。
     */
    WechatPayNotifyResult parseAndVerifyNotify(String requestBody, Map<String, String> headers);

    record WechatMiniappPaymentRequest(
        String description,
        String outTradeNo,
        Integer amountCents,
        String openId,
        String notifyUrl
    ) {
    }

    record WechatMiniappPaymentResult(
        String prepayId,
        String timeStamp,
        String nonceStr,
        String packageValue,
        String signType,
        String paySign
    ) {
    }

    record WechatPayNotifyResult(
        String outTradeNo,
        String transactionId,
        Integer amountCents,
        LocalDateTime paidTime
    ) {
    }
}
