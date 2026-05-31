package com.forest.business.common.event.recharge;

import java.time.LocalDateTime;

/**
 * 表示充值到账这一跨业务事实。
 *
 * <p>point 只消费该事件进行入账，避免 payment 或 recharge 直接操作积分账本。</p>
 */
public record RechargePaidEvent(
    Long rechargeOrderId,
    Long userId,
    Integer creditedPoints,
    Long paymentOrderId,
    LocalDateTime paidTime
) {
}
