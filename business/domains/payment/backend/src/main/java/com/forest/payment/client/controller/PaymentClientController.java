package com.forest.payment.client.controller;

import com.forest.payment.channel.PaymentChannelGateway;
import com.forest.payment.client.service.PaymentClientService;
import com.forest.payment.entity.PaymentOrderPO;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import com.forest.starter.auth.context.CurrentPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 暴露微信小程序支付接口。
 *
 * <p>支付域负责生成微信小程序支付参数，但不直接处理充值到账或积分入账。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.CLIENT + "/payment")
public class PaymentClientController {
    private final PaymentClientService paymentClientService;
    private final CurrentPrincipal currentAuth;

    public PaymentClientController(PaymentClientService paymentClientService, CurrentPrincipal currentAuth) {
        this.paymentClientService = paymentClientService;
        this.currentAuth = currentAuth;
    }

    @PostMapping("/orders")
    public Result<PaymentOrderVO> createOrder(@RequestBody CreatePaymentOrderRequest request) {
        // 根据业务单快照创建支付单，并返回 wx.requestPayment 所需参数。
        PaymentClientService.PaymentCreationResult result = paymentClientService.createOrder(
            currentAuth.requireUserId(),
            request.bizType(),
            request.bizOrderId()
        );
        return Result.success(PaymentOrderVO.from(result.paymentOrder(), result.paymentResult()));
    }

    @GetMapping("/orders/{id}")
    public Result<PaymentOrderVO> getOrder(@PathVariable Long id) {
        // 查询已有支付单时不返回支付参数，避免前端重复使用过期 prepay 参数。
        return Result.success(PaymentOrderVO.from(paymentClientService.getOrder(currentAuth.requireUserId(), id), null));
    }

    public record CreatePaymentOrderRequest(String bizType, Long bizOrderId) {
    }

    public record PaymentOrderVO(
        Long id,
        String paymentNo,
        String bizType,
        Long bizOrderId,
        String channel,
        Integer amountCents,
        String status,
        String outTradeNo,
        WechatMiniappPaymentParams paymentParams,
        LocalDateTime paidTime
    ) {
        public static PaymentOrderVO from(
            PaymentOrderPO order,
            PaymentChannelGateway.WechatMiniappPaymentResult paymentResult
        ) {
            return new PaymentOrderVO(
                order.getId(),
                order.getPaymentNo(),
                order.getBizType(),
                order.getBizOrderId(),
                order.getChannel().name(),
                order.getAmountCents(),
                order.getStatus().name(),
                order.getOutTradeNo(),
                WechatMiniappPaymentParams.from(paymentResult),
                order.getPaidTime()
            );
        }
    }

    public record WechatMiniappPaymentParams(
        String timeStamp,
        String nonceStr,
        String packageValue,
        String signType,
        String paySign
    ) {
        public static WechatMiniappPaymentParams from(PaymentChannelGateway.WechatMiniappPaymentResult paymentResult) {
            if (paymentResult == null) {
                return null;
            }
            return new WechatMiniappPaymentParams(
                paymentResult.timeStamp(),
                paymentResult.nonceStr(),
                paymentResult.packageValue(),
                paymentResult.signType(),
                paymentResult.paySign()
            );
        }
    }
}
