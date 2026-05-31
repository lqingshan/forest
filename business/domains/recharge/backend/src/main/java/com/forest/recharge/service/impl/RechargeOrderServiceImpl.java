package com.forest.recharge.service.impl;

import com.forest.business.common.event.recharge.RechargePaidEvent;
import com.forest.recharge.client.service.RechargeClientService;
import com.forest.recharge.entity.RechargeOrderPO;
import com.forest.recharge.packagecfg.RechargePackageCatalog;
import com.forest.recharge.packagecfg.RechargePackageDefinition;
import com.forest.recharge.repository.RechargeOrderRepository;
import com.forest.recharge.service.RechargeOrderService;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.time.ForestTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 实现充值主单能力。
 *
 * <p>充值域只表达“哪笔充值业务成功”，不直接调用微信支付，也不直接操作支付渠道。</p>
 */
@Service
public class RechargeOrderServiceImpl implements RechargeOrderService, RechargeClientService {
    private static final DateTimeFormatter RECHARGE_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RechargeOrderRepository rechargeOrderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RechargeOrderServiceImpl(
        RechargeOrderRepository rechargeOrderRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.rechargeOrderRepository = rechargeOrderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<RechargePackageDefinition> getPackages() {
        return RechargePackageCatalog.list();
    }

    @Override
    @Transactional
    public RechargeOrderPO createOrder(Long userId, String packageCode) {
        // 创建充值主单时锁定套餐金额和到账积分，后续支付只引用这张业务单快照。
        RechargePackageDefinition pkg = RechargePackageCatalog.getRequired(packageCode);
        RechargeOrderPO order = new RechargeOrderPO();
        order.setUserId(userId);
        order.setPackageCode(pkg.code());
        order.setAmountCents(pkg.amountCents());
        order.setCreditedPoints(pkg.creditedPoints());
        order.setStatus(RechargeOrderPO.Status.CREATED);
        order.setRechargeNo(nextRechargeNo());
        return rechargeOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public RechargeOrderPO getOrder(Long userId, Long orderId) {
        return getRequiredByIdAndUserId(orderId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public RechargeOrderPO getRequiredById(Long rechargeOrderId) {
        return rechargeOrderRepository.findById(rechargeOrderId)
            .orElseThrow(() -> new BusinessException("订单不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public RechargeOrderPO getRequiredByIdAndUserId(Long rechargeOrderId, Long userId) {
        return rechargeOrderRepository.findByIdAndUserId(rechargeOrderId, userId)
            .orElseThrow(() -> new BusinessException("订单不存在"));
    }

    @Override
    @Transactional
    public RechargeOrderPO markPaid(Long rechargeOrderId, Long paymentOrderId, LocalDateTime paidTime) {
        RechargeOrderPO order = getRequiredById(rechargeOrderId);

        // 微信支付可能重复通知；充值单已到账时直接返回，避免重复发布积分入账事件。
        if (order.getStatus() == RechargeOrderPO.Status.PAID) {
            return order;
        }
        if (order.getStatus() == RechargeOrderPO.Status.CLOSED) {
            return order;
        }

        order.setStatus(RechargeOrderPO.Status.PAID);
        order.setPaidPaymentOrderId(paymentOrderId);
        order.setPaidTime(paidTime == null ? ForestTime.now() : paidTime);
        RechargeOrderPO saved = rechargeOrderRepository.save(order);
        // 充值到账后通过事件通知 point 入账，payment 和 recharge 都不直接改积分账本。
        eventPublisher.publishEvent(new RechargePaidEvent(
            saved.getId(),
            saved.getUserId(),
            saved.getCreditedPoints(),
            saved.getPaidPaymentOrderId(),
            saved.getPaidTime()
        ));
        return saved;
    }

    private String nextRechargeNo() {
        int randomNumber = ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
        return "RECHARGE-" + ForestTime.now().format(RECHARGE_NO_TIME_FORMATTER) + "-" + randomNumber;
    }
}
