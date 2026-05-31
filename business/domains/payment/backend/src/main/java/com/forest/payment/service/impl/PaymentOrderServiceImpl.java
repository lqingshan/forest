package com.forest.payment.service.impl;

import com.forest.business.common.event.payment.PaymentSucceededEvent;
import com.forest.business.common.payment.PaymentBizResolver;
import com.forest.business.common.payment.PaymentBizSnapshot;
import com.forest.payment.channel.PaymentChannelGateway;
import com.forest.payment.client.service.PaymentClientService;
import com.forest.payment.config.WechatPayProperties;
import com.forest.payment.entity.PaymentOrderPO;
import com.forest.payment.repository.PaymentOrderRepository;
import com.forest.payment.service.PaymentOrderService;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.time.ForestTime;
import com.forest.user.account.service.AccountService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 实现支付单能力。
 *
 * <p>支付域只记录支付事实和渠道交互结果，不直接理解充值到账或积分入账。</p>
 */
@Service
public class PaymentOrderServiceImpl implements PaymentOrderService, PaymentClientService {
    private static final String WECHAT_ACCOUNT_TYPE = "wechat_miniapp";
    private static final DateTimeFormatter PAYMENT_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PaymentOrderRepository paymentOrderRepository;
    private final List<PaymentBizResolver> paymentBizResolvers;
    private final PaymentChannelGateway paymentChannelGateway;
    private final AccountService accountService;
    private final ApplicationEventPublisher eventPublisher;
    private final WechatPayProperties wechatPayProperties;

    public PaymentOrderServiceImpl(
        PaymentOrderRepository paymentOrderRepository,
        List<PaymentBizResolver> paymentBizResolvers,
        PaymentChannelGateway paymentChannelGateway,
        AccountService accountService,
        ApplicationEventPublisher eventPublisher,
        WechatPayProperties wechatPayProperties
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentBizResolvers = paymentBizResolvers;
        this.paymentChannelGateway = paymentChannelGateway;
        this.accountService = accountService;
        this.eventPublisher = eventPublisher;
        this.wechatPayProperties = wechatPayProperties;
    }

    @Override
    @Transactional
    public PaymentCreationResult createOrder(Long userId, String bizType, Long bizOrderId) {
        // 支付前先向业务域取快照，确保金额、描述和归属权都由业务主单决定。
        PaymentBizSnapshot snapshot = resolveBizSnapshot(userId, bizType, bizOrderId);
        closeActiveOrders(snapshot.bizType(), snapshot.bizOrderId());

        // 微信小程序支付必须使用当前小程序用户的 openid。
        String openId = accountService.getRequiredIdentifierByUserIdAndType(userId, WECHAT_ACCOUNT_TYPE);
        PaymentOrderPO order = new PaymentOrderPO();
        order.setPaymentNo(nextPaymentNo());
        order.setBizType(snapshot.bizType());
        order.setBizOrderId(snapshot.bizOrderId());
        order.setChannel(PaymentOrderPO.Channel.WECHAT_MINIAPP_PAYMENT);
        order.setAmountCents(snapshot.amountCents());
        order.setStatus(PaymentOrderPO.Status.CREATED);
        order.setOutTradeNo(nextOutTradeNo());
        order = paymentOrderRepository.save(order);

        // 真实模式会调用微信支付下单；mock 模式返回可识别的假支付参数。
        PaymentChannelGateway.WechatMiniappPaymentResult paymentResult = paymentChannelGateway.createWechatMiniappPaymentOrder(
            new PaymentChannelGateway.WechatMiniappPaymentRequest(
                snapshot.description(),
                order.getOutTradeNo(),
                snapshot.amountCents(),
                openId,
                wechatPayProperties.getNotifyUrl()
            )
        );

        order.setPrepayId(paymentResult.prepayId());
        order.setStatus(PaymentOrderPO.Status.PREPAY_CREATED);
        order = paymentOrderRepository.save(order);
        return new PaymentCreationResult(order, paymentResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrderPO getOrder(Long userId, Long paymentOrderId) {
        PaymentOrderPO order = getRequiredById(paymentOrderId);
        if (!belongsToUser(userId, order)) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrderPO getRequiredById(Long paymentOrderId) {
        return paymentOrderRepository.findById(paymentOrderId)
            .orElseThrow(() -> new BusinessException("支付单不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrderPO getRequiredById(Long userId, Long paymentOrderId) {
        return getOrder(userId, paymentOrderId);
    }

    @Override
    @Transactional
    public void handleWechatPayNotify(String requestBody, Map<String, String> headers) {
        // open 回调入口只在渠道适配器验签/解密后才会返回支付事实。
        PaymentChannelGateway.WechatPayNotifyResult notifyResult =
            paymentChannelGateway.parseAndVerifyNotify(requestBody, headers);

        PaymentOrderPO order = paymentOrderRepository.findByOutTradeNo(notifyResult.outTradeNo())
            .orElseThrow(() -> new BusinessException("支付单不存在"));

        if (!order.getAmountCents().equals(notifyResult.amountCents())) {
            throw new BusinessException("支付金额不匹配");
        }
        // 微信支付会重试通知，已成功的支付单直接返回，避免重复发布 PaymentSucceededEvent。
        if (order.getStatus() == PaymentOrderPO.Status.SUCCESS) {
            return;
        }

        order.setStatus(PaymentOrderPO.Status.SUCCESS);
        order.setTransactionId(notifyResult.transactionId());
        order.setNotifyTime(ForestTime.now());
        order.setPaidTime(notifyResult.paidTime());
        PaymentOrderPO saved = paymentOrderRepository.save(order);

        // payment 只发布“支付成功事实”；充值到账和积分入账由下游事件监听器完成。
        eventPublisher.publishEvent(new PaymentSucceededEvent(
            saved.getId(),
            saved.getBizType(),
            saved.getBizOrderId(),
            saved.getAmountCents(),
            saved.getChannel().name(),
            saved.getTransactionId(),
            saved.getPaidTime()
        ));
    }

    private PaymentBizSnapshot resolveBizSnapshot(Long userId, String bizType, Long bizOrderId) {
        return paymentBizResolvers.stream()
            .filter(resolver -> resolver.supports(bizType))
            .findFirst()
            .orElseThrow(() -> new BusinessException("业务类型不支持支付"))
            .getRequiredSnapshot(userId, bizOrderId);
    }

    private void closeActiveOrders(String bizType, Long bizOrderId) {
        // 同一业务单只保留最新支付尝试，避免用户重复拉起多个未完成 prepay。
        paymentOrderRepository.findByBizTypeAndBizOrderIdAndStatusIn(
            bizType,
            bizOrderId,
            List.of(PaymentOrderPO.Status.CREATED, PaymentOrderPO.Status.PREPAY_CREATED)
        ).forEach(order -> order.setStatus(PaymentOrderPO.Status.CLOSED));
    }

    private boolean belongsToUser(Long userId, PaymentOrderPO order) {
        return paymentBizResolvers.stream()
            .filter(resolver -> resolver.supports(order.getBizType()))
            .findFirst()
            .map(resolver -> resolver.belongsToUser(userId, order.getBizOrderId()))
            .orElse(false);
    }

    private String nextPaymentNo() {
        int randomNumber = ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
        return "PAY-" + ForestTime.now().format(PAYMENT_NO_TIME_FORMATTER) + "-" + randomNumber;
    }

    private String nextOutTradeNo() {
        int randomNumber = ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
        return "WX-" + ForestTime.now().format(PAYMENT_NO_TIME_FORMATTER) + "-" + randomNumber;
    }
}
