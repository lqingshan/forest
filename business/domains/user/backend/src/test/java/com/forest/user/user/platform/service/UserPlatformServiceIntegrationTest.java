package com.forest.user.user.platform.service;

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
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = UserPlatformServiceIntegrationTest.TestApplication.class)
@ActiveProfiles("test")
/**
 * 基于组装后的持久化层验证平台用户管理服务行为。
 */
class UserPlatformServiceIntegrationTest {
    @Autowired
    private UserPlatformService userPlatformService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanDatabase() {
        userAccountRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void freezeUserRejectsAdminNickname() {
        UserPO user = new UserPO();
        user.setName("admin");
        user.setStatus(UserPO.Status.ACTIVE);
        user = userRepository.save(user);
        Long userId = user.getId();

        BusinessException exception = assertThrows(BusinessException.class, () -> userPlatformService.freezeUser(userId));
        assertEquals("平台用户不能被冻结", exception.getMessage());
        assertEquals(UserPO.Status.ACTIVE, userRepository.findById(userId).orElseThrow().getStatus());
    }

    @Test
    void freezeUserAllowsNonAdminNickname() {
        UserPO user = new UserPO();
        user.setName("regular user");
        user.setStatus(UserPO.Status.ACTIVE);
        user = userRepository.save(user);

        userPlatformService.freezeUser(user.getId());

        assertEquals(UserPO.Status.FROZEN, userRepository.findById(user.getId()).orElseThrow().getStatus());
    }

    @Test
    void getCurrentPlatformProfileReturnsUserProfileAndAdminUsername() {
        UserPO user = new UserPO();
        user.setName("Platform Admin");
        user.setPhone("13800138000");
        user.setEmail("admin@forest.example");
        user = userRepository.save(user);

        AccountPO account = new AccountPO();
        account.setType("platform_password");
        account.setCredentialScope(AccountPO.GLOBAL_CREDENTIAL_SCOPE);
        account.setIdentifier("+8613800138000");
        account.setSecret("hash");
        account = accountRepository.save(account);

        UserAccountPO link = new UserAccountPO();
        link.setUserId(user.getId());
        link.setAccountId(account.getId());
        userAccountRepository.save(link);

        UserPlatformService.PlatformProfile profile = userPlatformService.getCurrentPlatformProfile(user.getId());

        assertEquals(user.getId(), profile.userId());
        assertEquals("+8613800138000", profile.loginName());
        assertEquals("Platform Admin", profile.name());
        assertEquals("13800138000", profile.phone());
        assertEquals("admin@forest.example", profile.email());
        assertEquals(UserPO.Status.ACTIVE.name(), profile.status());
    }

    @Test
    void getCurrentPlatformProfileFallsBackToPhoneWithoutPlatformPasswordAccount() {
        UserPO user = new UserPO();
        user.setName("Platform User");
        user.setPhone("+8613800138001");
        user.setEmail("user@forest.example");
        user = userRepository.save(user);

        UserPlatformService.PlatformProfile profile = userPlatformService.getCurrentPlatformProfile(user.getId());

        assertEquals(user.getId(), profile.userId());
        assertEquals("+8613800138001", profile.loginName());
        assertEquals("Platform User", profile.name());
        assertEquals("+8613800138001", profile.phone());
        assertEquals("user@forest.example", profile.email());
        assertEquals(UserPO.Status.ACTIVE.name(), profile.status());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {UserPO.class, UserAccountPO.class, AccountPO.class})
    @EnableJpaRepositories(basePackageClasses = {UserRepository.class, UserAccountRepository.class, AccountRepository.class})
    @ComponentScan(basePackageClasses = UserServiceImpl.class)
    /**
     * 提供平台用户管理服务集成测试所需的最小测试应用。
     */
    static class TestApplication {
    }
}
