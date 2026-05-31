package com.forest.business.common.payment;

/**
 * 表示创建支付单时所需的上游业务快照。
 */
public record PaymentBizSnapshot(
    String bizType,
    Long bizOrderId,
    Integer amountCents,
    String description
) {
}
