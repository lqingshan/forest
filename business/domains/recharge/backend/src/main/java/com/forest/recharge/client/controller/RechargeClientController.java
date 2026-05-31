package com.forest.recharge.client.controller;

import com.forest.recharge.client.service.RechargeClientService;
import com.forest.recharge.entity.RechargeOrderPO;
import com.forest.recharge.packagecfg.RechargePackageDefinition;
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
import java.util.List;

/**
 * 暴露微信小程序充值接口。
 *
 * <p>充值单是积分充值业务主单，支付单只表示后续的一次支付尝试。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.CLIENT + "/recharge")
public class RechargeClientController {
    private final RechargeClientService rechargeClientService;
    private final CurrentPrincipal currentAuth;

    public RechargeClientController(RechargeClientService rechargeClientService, CurrentPrincipal currentAuth) {
        this.rechargeClientService = rechargeClientService;
        this.currentAuth = currentAuth;
    }

    @GetMapping("/packages")
    public Result<List<RechargePackageVO>> getPackages() {
        // 套餐由充值域维护，前端不自行计算金额和到账积分。
        return Result.success(rechargeClientService.getPackages().stream()
            .map(RechargePackageVO::from)
            .toList());
    }

    @PostMapping("/orders")
    public Result<RechargeOrderVO> createOrder(@RequestBody CreateRechargeOrderRequest request) {
        // 这里只创建充值业务单，不直接调用微信支付。
        return Result.success(RechargeOrderVO.from(rechargeClientService.createOrder(currentAuth.requireUserId(), request.packageCode())));
    }

    @GetMapping("/orders/{id}")
    public Result<RechargeOrderVO> getOrder(@PathVariable Long id) {
        // 支付结果页通过查询充值单状态确认最终是否到账。
        return Result.success(RechargeOrderVO.from(rechargeClientService.getOrder(currentAuth.requireUserId(), id)));
    }

    public record CreateRechargeOrderRequest(String packageCode) {
    }

    public record RechargePackageVO(String code, String title, Integer amountCents, Integer creditedPoints) {
        public static RechargePackageVO from(RechargePackageDefinition pkg) {
            return new RechargePackageVO(pkg.code(), pkg.title(), pkg.amountCents(), pkg.creditedPoints());
        }
    }

    public record RechargeOrderVO(
        Long id,
        String rechargeNo,
        String packageCode,
        Integer amountCents,
        Integer creditedPoints,
        String status,
        Long paidPaymentOrderId,
        LocalDateTime createdTime,
        LocalDateTime paidTime
    ) {
        public static RechargeOrderVO from(RechargeOrderPO order) {
            return new RechargeOrderVO(
                order.getId(),
                order.getRechargeNo(),
                order.getPackageCode(),
                order.getAmountCents(),
                order.getCreditedPoints(),
                order.getStatus().name(),
                order.getPaidPaymentOrderId(),
                order.getCreatedTime(),
                order.getPaidTime()
            );
        }
    }
}
