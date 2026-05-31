package com.forest.user.user.service;

import com.forest.starter.exception.BusinessException;
import com.forest.user.account.entity.AccountPO;
import com.forest.user.account.repository.AccountRepository;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.repository.UserRepository;
import com.forest.user.user.service.impl.UserServiceImpl;
import com.forest.user.useraccount.entity.UserAccountPO;
import com.forest.user.useraccount.repository.UserAccountRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = UserServiceIntegrationTest.TestApplication.class)
@ActiveProfiles("test")
/**
 * 基于组装后的持久化层验证用户服务行为。
 */
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @BeforeEach
    void cleanDatabase() {
        userAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findOrCreateByAccountIdCreatesNeutralActiveUserAndBinding() {
        UserService.ResolveUserResult result = userService.findOrCreateByAccountId(1001L);

        UserPO user = result.user();
        UserAccountPO link = userAccountRepository.findByAccountId(1001L).orElseThrow();

        assertTrue(result.created());
        assertEquals("新用户", user.getName());
        assertEquals(UserPO.Status.ACTIVE, user.getStatus());
        assertEquals(user.getId(), link.getUserId());
    }

    @Test
    void findOrCreateByAccountIdIsIdempotentForSameAccount() {
        UserService.ResolveUserResult first = userService.findOrCreateByAccountId(1002L);
        UserService.ResolveUserResult second = userService.findOrCreateByAccountId(1002L);

        assertTrue(first.created());
        assertFalse(second.created());
        assertEquals(first.user().getId(), second.user().getId());
        assertEquals(1L, userRepository.count());
        assertEquals(1L, userAccountRepository.count());
    }

    @Test
    void findOrCreateByAccountIdRejectsFrozenUser() {
        UserPO user = new UserPO();
        user.setName("Frozen UserPO");
        user.setStatus(UserPO.Status.FROZEN);
        user = userRepository.save(user);

        UserAccountPO link = new UserAccountPO();
        link.setUserId(user.getId());
        link.setAccountId(1003L);
        userAccountRepository.save(link);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.findOrCreateByAccountId(1003L));
        assertEquals("用户已被冻结", exception.getMessage());
    }

    @Test
    void updateStatusPersistsNewStatus() {
        UserPO user = new UserPO();
        user.setName("Status UserPO");
        user = userRepository.save(user);

        UserPO updated = userService.updateStatus(user.getId(), UserPO.Status.FROZEN);

        assertEquals(UserPO.Status.FROZEN, updated.getStatus());
        assertEquals(UserPO.Status.FROZEN, userRepository.findById(user.getId()).orElseThrow().getStatus());
    }

    @Test
    void updateStatusRestoresFrozenUserToActive() {
        UserPO user = new UserPO();
        user.setName("Recovered UserPO");
        user.setStatus(UserPO.Status.FROZEN);
        user = userRepository.save(user);

        UserPO updated = userService.updateStatus(user.getId(), UserPO.Status.ACTIVE);

        assertEquals(UserPO.Status.ACTIVE, updated.getStatus());
        assertEquals(UserPO.Status.ACTIVE, userRepository.findById(user.getId()).orElseThrow().getStatus());
    }

    @Test
    void searchPageSupportsFuzzyMatchByName() {
        UserPO alice = new UserPO();
        alice.setName("Alice Buyer");
        userRepository.save(alice);

        UserPO bob = new UserPO();
        bob.setName("Bob Supplier");
        userRepository.save(bob);

        var result = userService.searchPage(
            new UserService.UserPageQuery(null, "buyer", null, null, null),
            PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Alice Buyer", result.getContent().get(0).getName());
    }

    @Test
    void searchPageSupportsFuzzyMatchByPhoneAndEmail() {
        UserPO matched = new UserPO();
        matched.setName("Contact Match");
        matched.setPhone("13800138000");
        matched.setEmail("buyer@forest.example");
        userRepository.save(matched);

        UserPO unmatched = new UserPO();
        unmatched.setName("Other Contact");
        unmatched.setPhone("13900139000");
        unmatched.setEmail("seller@forest.example");
        userRepository.save(unmatched);

        var phoneResult = userService.searchPage(
            new UserService.UserPageQuery(null, null, "800", null, null),
            PageRequest.of(0, 10)
        );
        var emailResult = userService.searchPage(
            new UserService.UserPageQuery(null, null, null, "BUYER@", null),
            PageRequest.of(0, 10)
        );

        assertEquals(1, phoneResult.getTotalElements());
        assertEquals(matched.getId(), phoneResult.getContent().get(0).getId());
        assertEquals(1, emailResult.getTotalElements());
        assertEquals(matched.getId(), emailResult.getContent().get(0).getId());
    }

    @Test
    void searchPageRejectsShortPhoneOrEmailKeyword() {
        BusinessException phoneException = assertThrows(
            BusinessException.class,
            () -> userService.searchPage(
                new UserService.UserPageQuery(null, null, "1", null, null),
                PageRequest.of(0, 10)
            )
        );
        BusinessException emailException = assertThrows(
            BusinessException.class,
            () -> userService.searchPage(
                new UserService.UserPageQuery(null, null, null, "a", null),
                PageRequest.of(0, 10)
            )
        );

        assertEquals("手机号或邮箱至少输入 2 个字符", phoneException.getMessage());
        assertEquals("手机号或邮箱至少输入 2 个字符", emailException.getMessage());
    }

    @Test
    void searchPageCombinesIdAndStatusFilters() {
        UserPO activeUser = new UserPO();
        activeUser.setName("Active User");
        activeUser.setStatus(UserPO.Status.ACTIVE);
        activeUser = userRepository.save(activeUser);

        UserPO frozenUser = new UserPO();
        frozenUser.setName("Frozen User");
        frozenUser.setStatus(UserPO.Status.FROZEN);
        frozenUser = userRepository.save(frozenUser);
        Long frozenUserId = frozenUser.getId();

        var result = userService.searchPage(
            new UserService.UserPageQuery(activeUser.getId(), "user", null, null, UserPO.Status.ACTIVE),
            PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(activeUser.getId(), result.getContent().get(0).getId());
        assertEquals(UserPO.Status.ACTIVE, result.getContent().get(0).getStatus());
        assertFalse(result.getContent().stream().anyMatch(user -> user.getId().equals(frozenUserId)));
    }

    @Test
    void searchIdsReturnsMatchingUsersWithoutPagination() {
        UserPO matched = new UserPO();
        matched.setName("Alice Buyer");
        matched.setPhone("13800138000");
        matched.setEmail("alice@forest.example");
        matched = userRepository.save(matched);

        UserPO unmatched = new UserPO();
        unmatched.setName("Bob Supplier");
        unmatched.setPhone("13900139000");
        unmatched.setEmail("bob@forest.example");
        userRepository.save(unmatched);

        List<Long> result = userService.searchIds(
            new UserService.UserPageQuery(null, "alice", null, null, null)
        );

        assertEquals(List.of(matched.getId()), result);
    }

    @Test
    void getUserMapReturnsRequestedUsers() {
        UserPO first = new UserPO();
        first.setName("Alice");
        first = userRepository.save(first);

        UserPO second = new UserPO();
        second.setName("Bob");
        second = userRepository.save(second);

        var result = userService.getUserMap(List.of(first.getId(), second.getId()));

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(first.getId()).getName());
        assertEquals("Bob", result.get(second.getId()).getName());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {UserPO.class, UserAccountPO.class, AccountPO.class})
    @EnableJpaRepositories(basePackageClasses = {UserRepository.class, UserAccountRepository.class, AccountRepository.class})
    @ComponentScan(basePackageClasses = UserServiceImpl.class)
    /**
     * 提供用户服务集成测试所需的最小测试应用。
     */
    static class TestApplication {
    }
}
