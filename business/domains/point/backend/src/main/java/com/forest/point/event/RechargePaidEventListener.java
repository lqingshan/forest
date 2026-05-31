package com.forest.point.event;

import com.forest.business.common.event.recharge.RechargePaidEvent;
import com.forest.point.entity.PointLogPO;
import com.forest.point.service.PointBalanceService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听充值到账事件并写入积分账本。
 *
 * <p>point 不消费 PaymentSucceededEvent，只有充值域确认到账后才增加用户积分。</p>
 */
@Component
public class RechargePaidEventListener {
    private final PointBalanceService pointBalanceService;

    public RechargePaidEventListener(PointBalanceService pointBalanceService) {
        this.pointBalanceService = pointBalanceService;
    }

    @EventListener
    public void handle(RechargePaidEvent event) {
        // bizKey 以充值单维度幂等，避免重复事件导致重复加积分。
        pointBalanceService.addPoints(
            event.userId(),
            event.creditedPoints(),
            PointLogPO.SourceType.RECHARGE,
            event.rechargeOrderId(),
            "recharge:" + event.rechargeOrderId()
        );
    }
}
