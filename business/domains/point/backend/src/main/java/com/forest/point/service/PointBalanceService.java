package com.forest.point.service;

import com.forest.point.entity.PointBalancePO;
import com.forest.point.entity.PointLogPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * 定义积分余额与积分流水能力。
 */
public interface PointBalanceService {
    PointBalancePO ensureBalance(Long userId);

    PointBalancePO getBalance(Long userId);

    PointBalanceSummary getBalanceSummary(Long userId);

    Map<Long, PointBalanceSummary> getBalanceSummaryMap(Collection<Long> userIds);

    Page<PointBalanceSummary> pageBalanceSummariesByUserIds(Collection<Long> userIds, Pageable pageable);

    Page<PointLogPO> getLogPage(Long userId, Pageable pageable);

    PointChangeResult addPoints(
        Long userId,
        Integer amount,
        PointLogPO.SourceType sourceType,
        Long sourceId,
        String bizKey
    );

    PointChangeResult spendPoints(
        Long userId,
        Integer amount,
        PointLogPO.SourceType sourceType,
        Long sourceId,
        String bizKey
    );

    /**
     * 表示一次积分余额变更结果。
     */
    record PointChangeResult(Integer balanceAfter, Long logId) {
    }

    /**
     * 表示只读积分摘要信息。
     */
    record PointBalanceSummary(
        Long userId,
        Integer balance,
        Integer totalIncome,
        Integer totalSpend,
        LocalDateTime updatedAt
    ) {
        public static PointBalanceSummary from(PointBalancePO balance) {
            return new PointBalanceSummary(
                balance.getUserId(),
                balance.getBalance(),
                balance.getTotalIncome(),
                balance.getTotalSpend(),
                balance.getModifiedTime()
            );
        }

        public static PointBalanceSummary zero(Long userId) {
            return new PointBalanceSummary(userId, 0, 0, 0, null);
        }
    }
}
