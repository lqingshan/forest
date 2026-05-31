package com.forest.recharge.service;

import com.forest.recharge.entity.RechargeOrderPO;

import java.time.LocalDateTime;

/**
 * 定义充值主单核心能力。
 */
public interface RechargeOrderService {
    RechargeOrderPO createOrder(Long userId, String packageCode);

    RechargeOrderPO getRequiredById(Long rechargeOrderId);

    RechargeOrderPO getRequiredByIdAndUserId(Long rechargeOrderId, Long userId);

    RechargeOrderPO markPaid(Long rechargeOrderId, Long paymentOrderId, LocalDateTime paidTime);
}
