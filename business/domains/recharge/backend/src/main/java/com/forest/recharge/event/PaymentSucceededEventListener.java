package com.forest.recharge.event;

import com.forest.business.common.event.payment.PaymentSucceededEvent;
import com.forest.recharge.service.RechargeOrderService;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

/**
 * 监听支付成功事件并推进充值主单到账。
 *
 * <p>事件链路：PaymentSucceededEvent -> RechargePaidEvent -> point 入账。</p>
 */
@Component
public class PaymentSucceededEventListener {
    private static final String RECHARGE_BIZ_TYPE = "RECHARGE";

    private final RechargeOrderService rechargeOrderService;

    public PaymentSucceededEventListener(RechargeOrderService rechargeOrderService) {
        this.rechargeOrderService = rechargeOrderService;
    }

    @EventListener
    public void handle(PaymentSucceededEvent event) {
        // payment 是通用域，只有 RECHARGE 业务类型才由充值域消费。
        if (!RECHARGE_BIZ_TYPE.equals(event.bizType())) {
            return;
        }

        rechargeOrderService.markPaid(event.bizOrderId(), event.paymentOrderId(), event.paidTime());
    }
}
