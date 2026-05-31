package com.forest.point.service.impl;

import com.forest.point.client.service.PointClientService;
import com.forest.point.entity.PointBalancePO;
import com.forest.point.entity.PointLogPO;
import com.forest.point.repository.PointBalanceRepository;
import com.forest.point.repository.PointLogRepository;
import com.forest.point.service.PointBalanceService;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.time.ForestTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 实现积分余额与积分流水能力。
 */
@Service
public class PointBalanceServiceImpl implements PointBalanceService, PointClientService {
    private final PointBalanceRepository pointBalanceRepository;
    private final PointLogRepository pointLogRepository;
    private final TransactionTemplate requiresNewTransaction;

    public PointBalanceServiceImpl(
        PointBalanceRepository pointBalanceRepository,
        PointLogRepository pointLogRepository,
        PlatformTransactionManager transactionManager
    ) {
        this.pointBalanceRepository = pointBalanceRepository;
        this.pointLogRepository = pointLogRepository;
        this.requiresNewTransaction = new TransactionTemplate(transactionManager);
        this.requiresNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    @Transactional
    public PointBalancePO ensureBalance(Long userId) {
        validateUserId(userId);
        PointBalancePO existing = pointBalanceRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            return existing;
        }

        tryCreateInitialBalance(userId);
        return pointBalanceRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException("积分余额初始化失败"));
    }

    @Override
    @Transactional
    public PointBalancePO getBalance(Long userId) {
        return ensureBalance(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PointBalanceSummary getBalanceSummary(Long userId) {
        validateUserId(userId);
        return pointBalanceRepository.findByUserId(userId)
            .map(PointBalanceSummary::from)
            .orElse(PointBalanceSummary.zero(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, PointBalanceSummary> getBalanceSummaryMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return pointBalanceRepository.findByUserIdIn(userIds).stream()
            .map(PointBalanceSummary::from)
            .collect(Collectors.toMap(PointBalanceSummary::userId, Function.identity()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointBalanceSummary> pageBalanceSummariesByUserIds(Collection<Long> userIds, Pageable pageable) {
        if (userIds == null || userIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(
                Sort.Order.desc("modifiedTime"),
                Sort.Order.desc("userId")
            )
        );
        return pointBalanceRepository.findByUserIdIn(userIds, sortedPageable)
            .map(PointBalanceSummary::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointLogPO> getLogPage(Long userId, Pageable pageable) {
        validateUserId(userId);
        return pointLogRepository.findByUserIdOrderByCreatedTimeDesc(userId, pageable);
    }

    @Override
    @Transactional
    public PointChangeResult addPoints(
        Long userId,
        Integer amount,
        PointLogPO.SourceType sourceType,
        Long sourceId,
        String bizKey
    ) {
        validateChangeRequest(userId, amount, sourceType, bizKey);

        // 充值回调可能重复到达，bizKey 先查重可以保证同一业务只入账一次。
        PointLogPO existing = pointLogRepository.findByBizKey(bizKey).orElse(null);
        if (existing != null) {
            return new PointChangeResult(existing.getBalanceAfter(), existing.getId());
        }

        PointBalancePO balance = ensureBalance(userId);
        for (int retry = 0; retry < 5; retry++) {
            // 使用版本号做乐观并发控制，避免多个入账/扣减请求覆盖余额。
            int updated = pointBalanceRepository.addBalance(
                userId,
                amount,
                balance.getVersion(),
                ForestTime.now()
            );
            if (updated == 1) {
                PointBalancePO latest = pointBalanceRepository.findByUserId(userId)
                    .orElseThrow(() -> new BusinessException("积分余额不存在"));
                PointLogPO log = createPointLog(
                    userId,
                    PointLogPO.Direction.INCOME,
                    amount,
                    latest.getBalance(),
                    sourceType,
                    sourceId,
                    bizKey
                );
                pointLogRepository.save(log);
                return new PointChangeResult(latest.getBalance(), log.getId());
            }
            balance = pointBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("积分余额不存在"));
        }

        throw new BusinessException("积分增加失败，请稍后重试");
    }

    @Override
    @Transactional
    public PointChangeResult spendPoints(
        Long userId,
        Integer amount,
        PointLogPO.SourceType sourceType,
        Long sourceId,
        String bizKey
    ) {
        validateChangeRequest(userId, amount, sourceType, bizKey);

        // 解锁动作可能被重复提交，bizKey 先查重可以避免重复扣积分。
        PointLogPO existing = pointLogRepository.findByBizKey(bizKey).orElse(null);
        if (existing != null) {
            return new PointChangeResult(existing.getBalanceAfter(), existing.getId());
        }

        PointBalancePO balance = ensureBalance(userId);
        for (int retry = 0; retry < 5; retry++) {
            // 扣减也走乐观并发控制，余额不足由 SQL 条件和重读后的余额共同兜底。
            int updated = pointBalanceRepository.spendBalance(
                userId,
                amount,
                balance.getVersion(),
                ForestTime.now()
            );
            if (updated == 1) {
                PointBalancePO latest = pointBalanceRepository.findByUserId(userId)
                    .orElseThrow(() -> new BusinessException("积分余额不存在"));
                PointLogPO log = createPointLog(
                    userId,
                    PointLogPO.Direction.SPEND,
                    amount,
                    latest.getBalance(),
                    sourceType,
                    sourceId,
                    bizKey
                );
                pointLogRepository.save(log);
                return new PointChangeResult(latest.getBalance(), log.getId());
            }
            balance = pointBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("积分余额不存在"));
            if (balance.getBalance() < amount) {
                throw new BusinessException("积分不足");
            }
        }

        throw new BusinessException("积分扣减失败，请稍后重试");
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户不存在");
        }
    }

    private void validateChangeRequest(Long userId, Integer amount, PointLogPO.SourceType sourceType, String bizKey) {
        validateUserId(userId);
        if (amount == null || amount <= 0) {
            throw new BusinessException("积分变动金额必须大于 0");
        }
        if (sourceType == null) {
            throw new BusinessException("积分来源不能为空");
        }
        if (bizKey == null || bizKey.isBlank()) {
            throw new BusinessException("积分业务键不能为空");
        }
    }

    private PointLogPO createPointLog(
        Long userId,
        PointLogPO.Direction direction,
        Integer amount,
        Integer balanceAfter,
        PointLogPO.SourceType sourceType,
        Long sourceId,
        String bizKey
    ) {
        PointLogPO log = new PointLogPO();
        log.setUserId(userId);
        log.setDirection(direction);
        log.setAmount(amount);
        log.setBalanceAfter(balanceAfter);
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setBizKey(bizKey);
        return log;
    }

    private void tryCreateInitialBalance(Long userId) {
        try {
            requiresNewTransaction.executeWithoutResult(status -> {
                PointBalancePO balance = new PointBalancePO();
                balance.setUserId(userId);
                pointBalanceRepository.saveAndFlush(balance);
            });
        } catch (DataIntegrityViolationException ignored) {
            // Another concurrent request already created the balance.
        }
    }

}
