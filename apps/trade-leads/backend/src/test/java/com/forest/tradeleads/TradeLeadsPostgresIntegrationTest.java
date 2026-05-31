package com.forest.tradeleads;

import com.forest.point.entity.PointBalancePO;
import com.forest.point.entity.PointLogPO;
import com.forest.point.repository.PointBalanceRepository;
import com.forest.point.repository.PointLogRepository;
import com.forest.point.service.PointBalanceService;
import com.forest.starter.exception.BusinessException;
import com.forest.user.auth.command.WechatMiniappLoginCommand;
import com.forest.user.auth.result.AuthLoginResult;
import com.forest.user.auth.service.AuthService;
import com.forest.user.account.repository.AccountRepository;
import com.forest.user.user.entity.UserPO;
import com.forest.user.useraccount.repository.UserAccountRepository;
import com.forest.user.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
/**
 * 基于真实数据库容器验证并发敏感流程。
 */
class TradeLeadsPostgresIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.3-alpine")
        .withDatabaseName("forest_test")
        .withUsername("forest")
        .withPassword("forest123");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.datasource.hikari.connection-init-sql", () -> "SET TIME ZONE 'Asia/Shanghai'");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("jwt.secret", () -> "test-secret-key-must-be-at-least-32-bytes-long");
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private PointBalanceService pointBalanceService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PointBalanceRepository pointBalanceRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        pointLogRepository.deleteAll();
        pointBalanceRepository.deleteAll();
        userAccountRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void postgresConnectionsUseAsiaShanghaiTimeZone() {
        assertEquals(
            "Asia/Shanghai",
            jdbcTemplate.queryForObject("select current_setting('TimeZone')", String.class)
        );
    }

    @Test
    void concurrentWechatLoginCreatesSingleAccountUserAndBalance() throws Exception {
        List<AuthLoginResult> results = runConcurrently(
            8,
            () -> authService.loginByWechatMiniapp(new WechatMiniappLoginCommand(
                "pg_concurrent_login",
                "WECHAT_MINIAPP",
                "trade-leads-miniapp",
                "CLIENT",
                "127.0.0.1",
                "postgres-integration-test"
            ))
        );

        assertEquals(8, results.size());
        assertEquals(1L, accountRepository.count());
        assertEquals(1L, userRepository.count());
        assertEquals(1L, userAccountRepository.count());
        assertEquals(1L, pointBalanceRepository.count());
    }

    @Test
    void concurrentEnsureBalanceCreatesSingleRow() throws Exception {
        UserPO user = new UserPO();
        user.setName("PG UserPO");
        user = userRepository.save(user);
        Long userId = user.getId();

        List<Long> balanceIds = runConcurrently(8, () -> pointBalanceService.ensureBalance(userId).getId());

        assertEquals(1L, pointBalanceRepository.count());
        assertEquals(1, balanceIds.stream().distinct().count());
    }

    @Test
    void concurrentSpendOnlyDeductsAvailableBalanceOnce() throws Exception {
        UserPO user = new UserPO();
        user.setName("Spend UserPO");
        user = userRepository.save(user);
        Long userId = user.getId();

        pointBalanceService.addPoints(userId, 5, PointLogPO.SourceType.RECHARGE, 1L, "seed:" + userId);

        AtomicInteger sequence = new AtomicInteger(1);
        List<SpendAttempt> attempts = runConcurrently(2, () -> {
            int index = sequence.getAndIncrement();
            try {
                pointBalanceService.spendPoints(
                    userId,
                    5,
                    PointLogPO.SourceType.UNLOCK,
                    (long) index,
                    "unlock:" + userId + ":" + index
                );
                return SpendAttempt.successful();
            } catch (BusinessException ex) {
                return SpendAttempt.failed(ex.getMessage());
            }
        });

        long successCount = attempts.stream().filter(SpendAttempt::success).count();
        long insufficientCount = attempts.stream().filter(result -> "积分不足".equals(result.message())).count();
        PointBalancePO balance = pointBalanceRepository.findByUserId(userId).orElseThrow();

        assertEquals(1L, successCount);
        assertEquals(1L, insufficientCount);
        assertEquals(0, balance.getBalance());
        assertEquals(5, balance.getTotalSpend());
        assertEquals(2L, pointLogRepository.count());
    }

    private <T> List<T> runConcurrently(int threadCount, Callable<T> task) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    assertTrue(start.await(5, TimeUnit.SECONDS));
                    return task.call();
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(10, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * 表示一次并发扣积分尝试的结果。
     */
    private record SpendAttempt(boolean success, String message) {
        private static SpendAttempt successful() {
            return new SpendAttempt(true, null);
        }

        private static SpendAttempt failed(String message) {
            return new SpendAttempt(false, message);
        }
    }
}
