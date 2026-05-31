package com.forest.business.common.payment;

/**
 * 定义支付域解析上游业务单快照的扩展点。
 */
public interface PaymentBizResolver {
    boolean supports(String bizType);

    PaymentBizSnapshot getRequiredSnapshot(Long userId, Long bizOrderId);

    boolean belongsToUser(Long userId, Long bizOrderId);
}
