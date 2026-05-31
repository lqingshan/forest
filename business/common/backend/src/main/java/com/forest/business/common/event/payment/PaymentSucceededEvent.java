package com.forest.business.common.event.payment;

import java.time.LocalDateTime;

/**
 * 表示支付单支付成功这一跨业务事实。
 *
 * <p>payment 只发布该事件，不直接调用充值或积分域；下游业务根据 bizType/bizOrderId 自行推进。</p>
 */
public record PaymentSucceededEvent(
    Long paymentOrderId,
    String bizType,
    Long bizOrderId,
    Integer amountCents,
    String channel,
    String transactionId,
    LocalDateTime paidTime
) {
}
