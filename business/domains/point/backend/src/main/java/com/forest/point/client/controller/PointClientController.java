package com.forest.point.client.controller;

import com.forest.point.client.service.PointClientService;
import com.forest.point.entity.PointBalancePO;
import com.forest.point.entity.PointLogPO;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import com.forest.starter.auth.context.CurrentPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露微信小程序积分余额与流水接口。
 *
 * <p>积分域只提供账本读写能力，不理解线索或充值页面的交互流程。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.CLIENT + "/point")
public class PointClientController {
    private final PointClientService pointClientService;
    private final CurrentPrincipal currentAuth;

    public PointClientController(PointClientService pointClientService, CurrentPrincipal currentAuth) {
        this.pointClientService = pointClientService;
        this.currentAuth = currentAuth;
    }

    @GetMapping("/balance")
    public Result<PointBalanceVO> getBalance() {
        // 余额不存在时会懒初始化，方便新用户登录后立即展示“我的”页面。
        PointBalancePO balance = pointClientService.getBalance(currentAuth.requireUserId());
        return Result.success(PointBalanceVO.from(balance));
    }

    @GetMapping("/logs/page")
    public Result<Page<PointLogVO>> getLogPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        // 流水用于用户核对充值入账和线索解锁扣减，按创建时间倒序返回。
        return Result.success(pointClientService
            .getLogPage(currentAuth.requireUserId(), PageRequest.of(page, size))
            .map(PointLogVO::from));
    }

    /**
     * 表示用户端当前积分汇总信息。
     */
    public record PointBalanceVO(Integer balance, Integer totalIncome, Integer totalSpend) {
        public static PointBalanceVO from(PointBalancePO balance) {
            return new PointBalanceVO(
                balance.getBalance(),
                balance.getTotalIncome(),
                balance.getTotalSpend()
            );
        }
    }

    /**
     * 表示用户端积分流水信息。
     */
    public record PointLogVO(
        Long id,
        Long userId,
        String direction,
        Integer amount,
        Integer balanceAfter,
        String sourceType,
        Long sourceId,
        String bizKey,
        java.time.LocalDateTime createdTime
    ) {
        public static PointLogVO from(PointLogPO log) {
            return new PointLogVO(
                log.getId(),
                log.getUserId(),
                log.getDirection().name(),
                log.getAmount(),
                log.getBalanceAfter(),
                log.getSourceType().name(),
                log.getSourceId(),
                log.getBizKey(),
                log.getCreatedTime()
            );
        }
    }
}
