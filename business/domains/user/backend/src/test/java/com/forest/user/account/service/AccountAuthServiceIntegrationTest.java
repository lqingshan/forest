package com.forest.user.account.service;

import com.forest.starter.carrierauth.CarrierAuthClient;
import com.forest.starter.carrierauth.CarrierAuthProvider;
import com.forest.starter.carrierauth.CarrierPhoneResolveResult;
import com.forest.user.auth.command.CarrierOneClickLoginCommand;
import com.forest.user.auth.command.PhonePasswordLoginCommand;
import com.forest.user.auth.command.PhoneSmsLoginCommand;
import com.forest.user.auth.command.WechatMiniappLoginCommand;
import com.forest.user.auth.command.WechatMiniappPhoneLoginCommand;
import com.forest.user.auth.result.AuthLoginResult;
import com.forest.user.auth.service.AuthService;
import com.forest.user.auth.service.AuthServiceImpl;
import com.forest.user.account.entity.AccountPO;
import com.forest.user.account.password.PasswordSecretCodec;
import com.forest.user.account.repository.AccountRepository;
import com.forest.user.account.service.impl.AccountServiceImpl;
import com.forest.user.account.token.JwtTokenProvider;
import com.forest.user.session.entity.AuthSessionPO;
import com.forest.user.session.entity.LoginLogPO;
import com.forest.user.session.model.LoginVerificationMode;
import com.forest.user.session.repository.AuthSessionRepository;
import com.forest.user.session.repository.LoginLogRepository;
import com.forest.user.session.service.AuthSessionService;
import com.forest.user.session.service.LoginLogService;
import com.forest.user.auth.service.PhoneNumberNormalizer;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.repository.UserRepository;
import com.forest.user.user.service.impl.UserServiceImpl;
import com.forest.user.useraccount.entity.UserAccountPO;
import com.forest.user.useraccount.repository.UserAccountRepository;
import com.forest.verification.service.SendSmsCodeCommand;
import com.forest.verification.service.SendSmsCodeResult;
import com.forest.verification.service.VerificationCodeMode;
import com.forest.verification.service.VerificationCodeService;
import com.forest.verification.service.VerifySmsCodeCommand;
import com.forest.verification.service.VerifySmsCodeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证账号中心登录主链路会同时落用户、账号、会话和登录日志。
 */
@SpringBootTest(classes = AccountAuthServiceIntegrationTest.TestApplication.class)
@ActiveProfiles("test")
class AccountAuthServiceIntegrationTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AuthSessionRepository authSessionRepository;

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordSecretCodec passwordSecretCodec;

    @BeforeEach
    void cleanDatabase() {
        loginLogRepository.deleteAll();
        authSessionRepository.deleteAll();
        userAccountRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void loginByPhoneWithSmsCodeCreatesSessionAndSuccessLog() {
        AuthLoginResult result = loginByPhone(
            "13800138000",
            "121314",
            "WECHAT_MINIAPP",
            "trade-leads-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.firstLogin()).isTrue();

        AccountPO phoneAccount = accountRepository
            .findByTypeAndCredentialScopeAndIdentifier("phone", AccountPO.GLOBAL_CREDENTIAL_SCOPE, "+8613800138000")
            .orElseThrow();
        UserPO user = userForAccount(phoneAccount);
        AuthSessionPO session = sessionFor(result);
        List<LoginLogPO> logs = loginLogRepository.findAll();

        assertThat(phoneAccount.getCredentialScope()).isEqualTo(AccountPO.GLOBAL_CREDENTIAL_SCOPE);
        assertThat(user.getPhone()).isEqualTo("+8613800138000");
        assertThat(session.getStatus()).isEqualTo(AuthSessionPO.Status.ACTIVE);
        assertThat(session.getClientType()).isEqualTo("WECHAT_MINIAPP");
        assertThat(session.getAppCode()).isEqualTo("trade-leads-miniapp");
        assertThat(session.getAccessScope()).isEqualTo("CLIENT");
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getResult()).isEqualTo(LoginLogPO.Result.SUCCESS);
        assertThat(logs.get(0).getVerificationMode()).isEqualTo(LoginVerificationMode.SMS_CODE);
    }

    @Test
    void loginByWechatDirectlyCreatesWechatAccountAndUserWithoutPhoneBinding() {
        AuthLoginResult result = loginByWechat(
            "wx-direct-1",
            "WECHAT_MINIAPP",
            "trade-leads-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );

        AccountPO wechatAccount = accountRepository
            .findByTypeAndCredentialScopeAndIdentifier("wechat_miniapp", "trade-leads-miniapp", "mock-openid-wx-direct-1")
            .orElseThrow();
        UserPO user = userForAccount(wechatAccount);

        assertThat(wechatAccount.getCredentialScope()).isEqualTo("trade-leads-miniapp");
        assertThat(user.getPhone()).isNull();
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(accountRepository.count()).isEqualTo(1);
        assertThat(userAccountRepository.findByUserId(user.getId()))
            .extracting(UserAccountPO::getAccountId)
            .containsExactly(wechatAccount.getId());
        assertThat(loginLogRepository.findAll())
            .singleElement()
            .satisfies(log -> {
                assertThat(log.getResult()).isEqualTo(LoginLogPO.Result.SUCCESS);
                assertThat(log.getVerificationMode()).isEqualTo(LoginVerificationMode.WECHAT_OPENID);
            });
    }

    @Test
    void loginByWechatBindsPhoneAccountWechatAccountAndOneUser() {
        AuthLoginResult result = loginByWechat(
            "wx-code-1",
            "13800138001",
            "WECHAT_MINIAPP",
            "trade-leads-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );

        AccountPO phoneAccount = accountRepository
            .findByTypeAndCredentialScopeAndIdentifier("phone", AccountPO.GLOBAL_CREDENTIAL_SCOPE, "+8613800138001")
            .orElseThrow();
        AccountPO wechatAccount = accountRepository
            .findByTypeAndCredentialScopeAndIdentifier("wechat_miniapp", "trade-leads-miniapp", "mock-openid-wx-code-1")
            .orElseThrow();
        UserPO user = userForAccount(wechatAccount);

        assertThat(user.getPhone()).isEqualTo("+8613800138001");
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(accountRepository.count()).isEqualTo(2);
        assertThat(userAccountRepository.findByUserId(user.getId()))
            .extracting(UserAccountPO::getAccountId)
            .containsExactlyInAnyOrder(phoneAccount.getId(), wechatAccount.getId());
        assertThat(loginLogRepository.findAll())
            .singleElement()
            .satisfies(log -> {
                assertThat(log.getResult()).isEqualTo(LoginLogPO.Result.SUCCESS);
                assertThat(log.getVerificationMode()).isEqualTo(LoginVerificationMode.WECHAT_PHONE);
            });
    }

    @Test
    void loginByCarrierTokenCreatesPhoneAccountUserAndSession() {
        AuthLoginResult result = loginByCarrierToken(
            "carrier-token-1",
            "ALIYUN",
            "ANDROID_APP",
            "cxc-commerce-buyer-android",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );

        AccountPO phoneAccount = accountRepository
            .findByTypeAndCredentialScopeAndIdentifier("phone", AccountPO.GLOBAL_CREDENTIAL_SCOPE, "+8613900139000")
            .orElseThrow();
        UserPO user = userForAccount(phoneAccount);
        AuthSessionPO session = sessionFor(result);

        assertThat(user.getPhone()).isEqualTo("+8613900139000");
        assertThat(session.getClientType()).isEqualTo("ANDROID_APP");
        assertThat(session.getAppCode()).isEqualTo("cxc-commerce-buyer-android");
        assertThat(session.getAccessScope()).isEqualTo("CLIENT");
        assertThat(loginLogRepository.findAll())
            .singleElement()
            .satisfies(log -> {
                assertThat(log.getResult()).isEqualTo(LoginLogPO.Result.SUCCESS);
                assertThat(log.getVerificationMode()).isEqualTo(LoginVerificationMode.CARRIER_ONE_CLICK);
            });
    }

    @Test
    void platformPhoneSmsLoginCreatesUserWithoutPlatformMarker() {
        AuthLoginResult result = loginByPhone(
            "13800138010",
            "121314",
            "PC_WEB",
            "trade-leads-platform-web",
            "PLATFORM",
            "127.0.0.1",
            "integration-test"
        );

        AccountPO phoneAccount = accountRepository.findByTypeAndCredentialScopeAndIdentifier(
            "phone",
            AccountPO.GLOBAL_CREDENTIAL_SCOPE,
            "+8613800138010"
        ).orElseThrow();
        UserPO user = userForAccount(phoneAccount);
        AuthSessionPO session = sessionFor(result);

        assertThat(result.firstLogin()).isTrue();
        assertThat(user.getPhone()).isEqualTo("+8613800138010");
        assertThat(session.getAccessScope()).isEqualTo("PLATFORM");
        assertThat(session.getUserId()).isEqualTo(user.getId());
    }

    @Test
    void platformPhoneSmsAndPasswordLoginDoNotRequirePlatformMarker() {
        UserPO user = createUser("平台用户", "+8613800138011");
        bindAccount(user.getId(), createAccount("phone", "+8613800138011", null));
        bindAccount(user.getId(), createAccount("phone_password", "+8613800138011", passwordSecretCodec.encode("secret")));

        AuthLoginResult smsResult = loginByPhone(
            "13800138011",
            "121314",
            "PC_WEB",
            "trade-leads-platform-web",
            "PLATFORM",
            "127.0.0.1",
            "integration-test"
        );
        AuthLoginResult passwordResult = loginByPhonePassword(
            "13800138011",
            "secret",
            "PC_WEB",
            "trade-leads-platform-web",
            "PLATFORM",
            "127.0.0.1",
            "integration-test"
        );

        assertThat(sessionFor(smsResult).getUserId()).isEqualTo(user.getId());
        assertThat(sessionFor(smsResult).getAccessScope()).isEqualTo("PLATFORM");
        assertThat(sessionFor(passwordResult).getUserId()).isEqualTo(user.getId());
        assertThat(sessionFor(passwordResult).getAccessScope()).isEqualTo("PLATFORM");
        assertThat(loginLogRepository.findAll())
            .extracting(LoginLogPO::getResult)
            .containsOnly(LoginLogPO.Result.SUCCESS);
    }

    @Test
    void loginByWechatPhoneBindsExistingWechatUserToNewPhoneAccount() {
        AuthLoginResult wechatResult = loginByWechat(
            "wx-existing",
            "WECHAT_MINIAPP",
            "trade-leads-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );

        AuthLoginResult phoneResult = loginByWechat(
            "wx-existing",
            "13800138002",
            "WECHAT_MINIAPP",
            "trade-leads-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );

        AccountPO phoneAccount = accountRepository
            .findByTypeAndCredentialScopeAndIdentifier("phone", AccountPO.GLOBAL_CREDENTIAL_SCOPE, "+8613800138002")
            .orElseThrow();
        UserPO user = userForAccount(phoneAccount);

        assertThat(sessionFor(phoneResult).getUserId()).isEqualTo(sessionFor(wechatResult).getUserId());
        assertThat(user.getPhone()).isEqualTo("+8613800138002");
        AccountPO wechatAccount = accountRepository
            .findByTypeAndCredentialScopeAndIdentifier("wechat_miniapp", "trade-leads-miniapp", "mock-openid-wx-existing")
            .orElseThrow();
        assertThat(userAccountRepository.findByUserId(user.getId()))
            .extracting(UserAccountPO::getAccountId)
            .contains(phoneAccount.getId(), wechatAccount.getId());
    }

    @Test
    void sameWechatOpenIdCanExistInDifferentCredentialScopes() {
        AuthLoginResult firstResult = loginByWechat(
            "same-openid",
            "WECHAT_MINIAPP",
            "purchase-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );
        AuthLoginResult secondResult = loginByWechat(
            "same-openid",
            "WECHAT_MINIAPP",
            "supplier-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );

        assertThat(sessionFor(firstResult).getAccountId()).isNotEqualTo(sessionFor(secondResult).getAccountId());
        assertThat(accountRepository.findByTypeAndIdentifierOrderByIdAsc("wechat_miniapp", "mock-openid-same-openid"))
            .extracting(AccountPO::getCredentialScope)
            .containsExactly("purchase-miniapp", "supplier-miniapp");
    }

    @Test
    void loginByWechatPhoneRejectsWhenWechatAndPhoneBelongToDifferentUsers() {
        AuthLoginResult phoneResult = loginByPhone(
            "13800138003",
            "121314",
            "WECHAT_MINIAPP",
            "trade-leads-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );
        AuthLoginResult wechatResult = loginByWechat(
            "wx-conflict",
            "WECHAT_MINIAPP",
            "trade-leads-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        );

        assertThat(sessionFor(phoneResult).getUserId()).isNotEqualTo(sessionFor(wechatResult).getUserId());
        assertThatThrownBy(() -> loginByWechat(
            "wx-conflict",
            "13800138003",
            "WECHAT_MINIAPP",
            "trade-leads-miniapp",
            "CLIENT",
            "127.0.0.1",
            "integration-test"
        ))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("微信账号已绑定其他手机号");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {
        AccountPO.class,
        UserPO.class,
        UserAccountPO.class,
        AuthSessionPO.class,
        LoginLogPO.class
    })
    @EnableJpaRepositories(basePackageClasses = {
        AccountRepository.class,
        UserRepository.class,
        UserAccountRepository.class,
        AuthSessionRepository.class,
        LoginLogRepository.class
    })
    @Import({
        AccountServiceImpl.class,
        AuthServiceImpl.class,
        UserServiceImpl.class,
        AuthSessionService.class,
        LoginLogService.class,
        PasswordSecretCodec.class,
        PhoneNumberNormalizer.class,
        JwtTokenProvider.class
    })
    static class TestApplication {
        @org.springframework.context.annotation.Bean
        CarrierAuthClient carrierAuthClient() {
            return request -> CarrierPhoneResolveResult.success(
                CarrierAuthProvider.MOCK,
                "+8613900139000",
                "mock-carrier-request-id",
                "OK",
                "mock carrier auth resolved"
            );
        }

        @org.springframework.context.annotation.Bean
        VerificationCodeService verificationCodeService() {
            return new VerificationCodeService() {
                @Override
                public SendSmsCodeResult sendSmsCode(SendSmsCodeCommand command) {
                    return new SendSmsCodeResult(command.phone(), Duration.ofMinutes(5));
                }

                @Override
                public VerifySmsCodeResult verifySmsCode(VerifySmsCodeCommand command) {
                    return new VerifySmsCodeResult(command.phone(), VerificationCodeMode.SMS_CODE);
                }
            };
        }
    }

    private AuthSessionPO sessionFor(AuthLoginResult result) {
        return authSessionRepository.findBySessionNo(jwtTokenProvider.getSessionNoFromToken(result.accessToken()))
            .orElseThrow();
    }

    private AuthLoginResult loginByPhone(
        String phone,
        String smsCode,
        String clientType,
        String appCode,
        String accessScope,
        String ip,
        String userAgent
    ) {
        return authService.loginByPhoneSms(new PhoneSmsLoginCommand(phone, smsCode, clientType, appCode, accessScope, ip, userAgent));
    }

    private AuthLoginResult loginByPhonePassword(
        String phone,
        String password,
        String clientType,
        String appCode,
        String accessScope,
        String ip,
        String userAgent
    ) {
        return authService.loginByPhonePassword(new PhonePasswordLoginCommand(phone, password, clientType, appCode, accessScope, ip, userAgent));
    }

    private AuthLoginResult loginByCarrierToken(
        String carrierToken,
        String provider,
        String clientType,
        String appCode,
        String accessScope,
        String ip,
        String userAgent
    ) {
        return authService.loginByCarrierToken(new CarrierOneClickLoginCommand(
            carrierToken,
            provider,
            clientType,
            appCode,
            accessScope,
            ip,
            userAgent
        ));
    }

    private AuthLoginResult loginByWechat(
        String code,
        String clientType,
        String appCode,
        String accessScope,
        String ip,
        String userAgent
    ) {
        return authService.loginByWechatMiniapp(new WechatMiniappLoginCommand(code, clientType, appCode, accessScope, ip, userAgent));
    }

    private AuthLoginResult loginByWechat(
        String code,
        String phoneCode,
        String clientType,
        String appCode,
        String accessScope,
        String ip,
        String userAgent
    ) {
        return authService.loginByWechatMiniappPhone(new WechatMiniappPhoneLoginCommand(
            code,
            phoneCode,
            clientType,
            appCode,
            accessScope,
            ip,
            userAgent
        ));
    }

    private UserPO userForAccount(AccountPO account) {
        UserAccountPO link = userAccountRepository.findByAccountId(account.getId()).orElseThrow();
        return userRepository.findById(link.getUserId()).orElseThrow();
    }

    private UserPO createUser(String name, String phone) {
        UserPO user = new UserPO();
        user.setName(name);
        user.setPhone(phone);
        user.setStatus(UserPO.Status.ACTIVE);
        return userRepository.save(user);
    }

    private AccountPO createAccount(String type, String identifier, String secret) {
        AccountPO account = new AccountPO();
        account.setType(type);
        account.setCredentialScope(AccountPO.GLOBAL_CREDENTIAL_SCOPE);
        account.setIdentifier(identifier);
        account.setSecret(secret);
        account.setStatus(AccountPO.Status.ACTIVE);
        return accountRepository.save(account);
    }

    private void bindAccount(Long userId, AccountPO account) {
        UserAccountPO link = new UserAccountPO();
        link.setUserId(userId);
        link.setAccountId(account.getId());
        userAccountRepository.save(link);
    }
}
