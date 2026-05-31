package com.forest.recharge.payment;

import com.forest.business.common.payment.PaymentBizResolver;
import com.forest.business.common.payment.PaymentBizSnapshot;
import com.forest.recharge.entity.RechargeOrderPO;
import com.forest.recharge.service.RechargeOrderService;
import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 为支付域提供充值业务单快照。
 *
 * <p>payment 只认识 bizType/bizOrderId；充值域通过该解析器提供金额、描述和归属权校验。</p>
 */
@Component
public class RechargePaymentBizResolver implements PaymentBizResolver {
    public static final String RECHARGE_BIZ_TYPE = "RECHARGE";

    private final RechargeOrderService rechargeOrderService;

    public RechargePaymentBizResolver(RechargeOrderService rechargeOrderService) {
        this.rechargeOrderService = rechargeOrderService;
    }

    @Override
    public boolean supports(String bizType) {
        return RECHARGE_BIZ_TYPE.equals(bizType);
    }

    @Override
    public PaymentBizSnapshot getRequiredSnapshot(Long userId, Long bizOrderId) {
        // 支付金额必须来自充值主单快照，不能由小程序前端传入。
        RechargeOrderPO order = rechargeOrderService.getRequiredByIdAndUserId(bizOrderId, userId);
        if (order.getStatus() != RechargeOrderPO.Status.CREATED) {
            throw new BusinessException("订单状态不可支付");
        }
        return new PaymentBizSnapshot(
            RECHARGE_BIZ_TYPE,
            order.getId(),
            order.getAmountCents(),
            "积分充值-" + order.getPackageCode()
        );
    }

    @Override
    public boolean belongsToUser(Long userId, Long bizOrderId) {
        try {
            rechargeOrderService.getRequiredByIdAndUserId(bizOrderId, userId);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }
}
