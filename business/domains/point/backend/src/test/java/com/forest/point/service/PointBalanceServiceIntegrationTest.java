package com.forest.point.service;

import com.forest.point.entity.PointBalancePO;
import com.forest.point.entity.PointLogPO;
import com.forest.point.repository.PointBalanceRepository;
import com.forest.point.repository.PointLogRepository;
import com.forest.point.service.impl.PointBalanceServiceImpl;
import com.forest.starter.exception.BusinessException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = PointBalanceServiceIntegrationTest.TestApplication.class)
@ActiveProfiles("test")
/**
 * 基于组装后的持久化层验证积分服务行为。
 */
class PointBalanceServiceIntegrationTest {
    @Autowired
    private PointBalanceService pointBalanceService;

    @Autowired
    private PointBalanceRepository pointBalanceRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void cleanDatabase() {
        pointLogRepository.deleteAll();
        pointBalanceRepository.deleteAll();
    }

    @Test
    void ensureBalanceIsIdempotentForSameUser() {
        PointBalancePO first = pointBalanceService.ensureBalance(2001L);
        PointBalancePO second = pointBalanceService.ensureBalance(2001L);

        assertEquals(first.getId(), second.getId());
        assertEquals(1L, pointBalanceRepository.count());
        assertEquals(0, second.getBalance());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void addPointsRejectsNonPositiveAmount(int amount) {
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> pointBalanceService.addPoints(2002L, amount, PointLogPO.SourceType.RECHARGE, 1L, "recharge:" + amount)
        );

        assertEquals("积分变动金额必须大于 0", exception.getMessage());
    }

    @Test
    void spendPointsRejectsBlankBizKey() {
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> pointBalanceService.spendPoints(2003L, 1, PointLogPO.SourceType.UNLOCK, 1L, " ")
        );

        assertEquals("积分业务键不能为空", exception.getMessage());
    }

    @Test
    void spendPointsRejectsWhenBalanceIsInsufficient() {
        pointBalanceService.ensureBalance(2004L);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> pointBalanceService.spendPoints(2004L, 1, PointLogPO.SourceType.UNLOCK, 1L, "unlock:2004:1")
        );

        assertEquals("积分不足", exception.getMessage());
    }

    @Test
    void addPointsIsIdempotentByBizKey() {
        PointBalanceService.PointChangeResult first =
            pointBalanceService.addPoints(2005L, 10, PointLogPO.SourceType.RECHARGE, 1L, "recharge:2005:1");
        PointBalanceService.PointChangeResult second =
            pointBalanceService.addPoints(2005L, 10, PointLogPO.SourceType.RECHARGE, 1L, "recharge:2005:1");

        PointBalancePO balance = pointBalanceRepository.findByUserId(2005L).orElseThrow();

        assertEquals(first.logId(), second.logId());
        assertEquals(10, first.balanceAfter());
        assertEquals(10, second.balanceAfter());
        assertEquals(10, balance.getBalance());
        assertEquals(10, balance.getTotalIncome());
        assertEquals(1L, pointLogRepository.count());
    }

    @Test
    void pageBalanceSummariesOrdersByModifiedTimeDescThenUserIdDesc() {
        PointBalancePO first = pointBalanceService.ensureBalance(2006L);
        PointBalancePO second = pointBalanceService.ensureBalance(2007L);
        PointBalancePO third = pointBalanceService.ensureBalance(2008L);

        LocalDateTime sharedModifiedTime = LocalDateTime.of(2026, 4, 14, 12, 0);

        addBalanceWithModifiedTime(first, 5, sharedModifiedTime.minusHours(1));
        addBalanceWithModifiedTime(second, 8, sharedModifiedTime);
        addBalanceWithModifiedTime(third, 13, sharedModifiedTime);

        var result = pointBalanceService.pageBalanceSummariesByUserIds(
            java.util.List.of(2006L, 2007L, 2008L),
            PageRequest.of(0, 3)
        );

        assertEquals(3, result.getTotalElements());
        assertEquals(2008L, result.getContent().get(0).userId());
        assertEquals(2007L, result.getContent().get(1).userId());
        assertEquals(2006L, result.getContent().get(2).userId());
    }

    private void addBalanceWithModifiedTime(PointBalancePO balance, int amount, LocalDateTime modifiedTime) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> assertEquals(
            1,
            pointBalanceRepository.addBalance(balance.getUserId(), amount, balance.getVersion(), modifiedTime)
        ));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {PointBalancePO.class, PointLogPO.class})
    @EnableJpaRepositories(basePackageClasses = {PointBalanceRepository.class, PointLogRepository.class})
    @ComponentScan(basePackageClasses = PointBalanceServiceImpl.class)
    /**
     * 提供积分服务集成测试所需的最小测试应用。
     */
    static class TestApplication {
    }
}
