package com.forest.user.auth.service;

import com.forest.business.common.event.account.AccountLoginSide;
import com.forest.business.common.event.account.AccountLoginSucceededEvent;
import com.forest.starter.auth.AccessScope;
import com.forest.starter.carrierauth.CarrierAuthClient;
import com.forest.starter.carrierauth.CarrierPhoneResolveRequest;
import com.forest.starter.carrierauth.CarrierPhoneResolveResult;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.wechat.miniapp.WechatMiniappClient;
import com.forest.user.auth.command.CarrierOneClickLoginCommand;
import com.forest.user.auth.command.PhonePasswordLoginCommand;
import com.forest.user.auth.command.PhoneSmsLoginCommand;
import com.forest.user.auth.command.SendSmsCodeCommand;
import com.forest.user.auth.command.WechatMiniappLoginCommand;
import com.forest.user.auth.command.WechatMiniappPhoneLoginCommand;
import com.forest.user.auth.result.AuthLoginResult;
import com.forest.user.auth.result.AuthRefreshResult;
import com.forest.user.auth.result.SmsSendResult;
import com.forest.user.account.entity.AccountPO;
import com.forest.user.account.password.PasswordSecretCodec;
import com.forest.user.account.service.AccountEnsureResult;
import com.forest.user.account.service.AccountService;
import com.forest.user.account.token.JwtTokenProvider;
import com.forest.user.session.entity.AuthSessionPO;
import com.forest.user.session.service.AuthSessionService;
import com.forest.user.session.service.LoginLogService;
import com.forest.user.session.service.LoginRequestContext;
import com.forest.user.session.model.LoginVerificationMode;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.service.UserService;
import com.forest.user.useraccount.entity.UserAccountPO;
import com.forest.user.useraccount.repository.UserAccountRepository;
import com.forest.verification.service.SendSmsCodeResult;
import com.forest.verification.service.VerificationCodeService;
import com.forest.verification.service.VerificationScene;
import com.forest.verification.service.VerifySmsCodeCommand;
import com.forest.verification.service.VerifySmsCodeResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 实现统一认证 API 编排。
 *
 * <p>这里只组合不同登录方式需要的账号、用户、验证码、微信、号码认证和会话能力；
 * 业务准入通过 {@link LoginAccessGuard} 扩展点挂接。</p>
 */
@Service
public class AuthServiceImpl implements AuthService {
    private static final String PHONE_ACCOUNT_TYPE = "phone";
    private static final String PHONE_PASSWORD_ACCOUNT_TYPE = "phone_password";
    private static final String WECHAT_ACCOUNT_TYPE = "wechat_miniapp";

    private final AccountService accountService;
    private final CarrierAuthClient carrierAuthClient;
    private final WechatMiniappClient wechatMiniappClient;
    private final UserService userService;
    private final UserAccountRepository userAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeService verificationCodeService;
    private final AuthSessionService authSessionService;
    private final LoginLogService loginLogService;
    private final PasswordSecretCodec passwordSecretCodec;
    private final PhoneNumberNormalizer phoneNumberNormalizer;
    private final ApplicationEventPublisher eventPublisher;
    private final List<LoginAccessGuard> loginAccessGuards;

    /**
     * 注入账号登录所需的协作服务。
     */
    public AuthServiceImpl(
        AccountService accountService,
        CarrierAuthClient carrierAuthClient,
        WechatMiniappClient wechatMiniappClient,
        UserService userService,
        UserAccountRepository userAccountRepository,
        JwtTokenProvider jwtTokenProvider,
        VerificationCodeService verificationCodeService,
        AuthSessionService authSessionService,
        LoginLogService loginLogService,
        PasswordSecretCodec passwordSecretCodec,
        PhoneNumberNormalizer phoneNumberNormalizer,
        ApplicationEventPublisher eventPublisher,
        List<LoginAccessGuard> loginAccessGuards
    ) {
        this.accountService = accountService;
        this.carrierAuthClient = carrierAuthClient;
        this.wechatMiniappClient = wechatMiniappClient;
        this.userService = userService;
        this.userAccountRepository = userAccountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.verificationCodeService = verificationCodeService;
        this.authSessionService = authSessionService;
        this.loginLogService = loginLogService;
        this.passwordSecretCodec = passwordSecretCodec;
        this.phoneNumberNormalizer = phoneNumberNormalizer;
        this.eventPublisher = eventPublisher;
        this.loginAccessGuards = loginAccessGuards;
    }

    /**
     * 发送登录短信验证码。
     *
     * <p>这里只做账号服务层编排，验证码内容、Redis 缓存、限流由 verification 领域负责。</p>
     */
    @Override
    public SmsSendResult sendSmsCode(SendSmsCodeCommand command) {
        LoginRequestContext context = new LoginRequestContext(
            command.clientType(),
            command.appCode(),
            command.accessScope(),
            command.ip(),
            null
        );
        requireClientApp(context);
        String normalizedPhone = phoneNumberNormalizer.normalizeChinaPhone(command.phone());
        SendSmsCodeResult result = verificationCodeService.sendSmsCode(new com.forest.verification.service.SendSmsCodeCommand(
            null,
            context.safeAppCode(),
            context.safeClientType(),
            VerificationScene.LOGIN,
            normalizedPhone,
            command.ip()
        ));
        return new SmsSendResult(result.phone(), result.ttlMinutes());
    }

    /**
     * 手机号验证码登录。
     *
     * <p>流程：校验 smsCode -> 创建或复用 phone/GLOBAL account -> 解析或创建 user ->
     * 确保 user.phone 主手机号 -> 创建 auth_session 和 token -> 写入登录日志和登录成功事件。</p>
     */
    @Override
    @Transactional
    public AuthLoginResult loginByPhoneSms(PhoneSmsLoginCommand command) {
        LoginRequestContext context = new LoginRequestContext(
            command.clientType(),
            command.appCode(),
            command.accessScope(),
            command.ip(),
            command.userAgent()
        );
        String normalizedPhone = command.phone();
        try {
            requireClientApp(context);
            normalizedPhone = phoneNumberNormalizer.normalizeChinaPhone(command.phone());
            VerifySmsCodeResult verifyResult = verificationCodeService.verifySmsCode(new VerifySmsCodeCommand(
                null,
                VerificationScene.LOGIN,
                normalizedPhone,
                command.smsCode()
            ));
            normalizedPhone = verifyResult.phone();
            AccountEnsureResult accountResult = accountService.ensureAccount(
                PHONE_ACCOUNT_TYPE,
                AccountService.GLOBAL_CREDENTIAL_SCOPE,
                normalizedPhone
            );
            UserResolution resolution = resolveUserByPhoneAccount(accountResult.account(), normalizedPhone);
            AuthLoginResult result = createLoginResult(
                resolution.user(),
                accountResult.account(),
                context,
                accountResult.created(),
                resolution.created()
            );
            loginLogService.recordSuccess(
                resolution.user(),
                accountResult.account(),
                requireResultSession(result),
                normalizedPhone,
                normalizedPhone,
                LoginVerificationMode.SMS_CODE,
                context
            );
            publishLoginSucceeded(
                resolution.user().getId(),
                accountResult.account().getId(),
                accountResult.account().getType(),
                loginSide(context),
                accountResult.created(),
                resolution.created()
            );
            return result;
        } catch (BusinessException ex) {
            loginLogService.recordFailure(
                PHONE_ACCOUNT_TYPE,
                normalizedPhone,
                normalizedPhone,
                null,
                ex.getMessage(),
                context
            );
            throw ex;
        }
    }

    /**
     * 手机号密码登录。
     *
     * <p>该登录方式只校验 phone_password/GLOBAL account；accessScope 只决定最终 session
     * 能访问哪个 API 前缀，不在账号服务里承担业务准入判断。</p>
     */
    @Override
    @Transactional
    public AuthLoginResult loginByPhonePassword(PhonePasswordLoginCommand command) {
        LoginRequestContext context = new LoginRequestContext(
            command.clientType(),
            command.appCode(),
            command.accessScope(),
            command.ip(),
            command.userAgent()
        );
        String normalizedPhone = command.phone();
        try {
            requireClientApp(context);
            normalizedPhone = phoneNumberNormalizer.normalizeChinaPhone(command.phone());
            AccountPO account = getRequiredUserPasswordAccount(normalizedPhone);
            if (!passwordSecretCodec.matches(command.password(), account.getSecret())) {
                throw new BusinessException("手机号或密码错误");
            }

            UserPO user = userService.findByAccountId(account.getId())
                .map(resolved -> userService.getRequiredActiveById(resolved.getId()))
                .orElseThrow(() -> new BusinessException("手机号或密码错误"));
            AuthLoginResult result = createLoginResult(user, account, context, false, false);
            loginLogService.recordSuccess(
                user,
                account,
                requireResultSession(result),
                normalizedPhone,
                user.getPhone(),
                LoginVerificationMode.PASSWORD,
                context
            );
            publishLoginSucceeded(user.getId(), account.getId(), account.getType(), loginSide(context), false, false);
            return result;
        } catch (BusinessException ex) {
            loginLogService.recordFailure(
                PHONE_PASSWORD_ACCOUNT_TYPE,
                normalizedPhone,
                normalizedPhone,
                LoginVerificationMode.PASSWORD,
                ex.getMessage(),
                context
            );
            throw ex;
        }
    }

    /**
     * APP 原生本机号一键登录。
     *
     * <p>业务层只接收原生号码认证 SDK 返回的一次性 carrierToken，然后通过
     * {@link CarrierAuthClient} 把 token 换成可信手机号；不会相信前端直接传入的手机号。</p>
     */
    @Override
    @Transactional
    public AuthLoginResult loginByCarrierToken(CarrierOneClickLoginCommand command) {
        LoginRequestContext context = new LoginRequestContext(
            command.clientType(),
            command.appCode(),
            command.accessScope(),
            command.ip(),
            command.userAgent()
        );
        String normalizedPhone = null;
        try {
            requireClientApp(context);
            CarrierPhoneResolveResult phoneResult = carrierAuthClient.resolvePhone(new CarrierPhoneResolveRequest(
                command.carrierToken(),
                command.provider(),
                context.safeAppCode() + "-" + jwtTokenProvider.newTokenId()
            ));
            if (!phoneResult.success()) {
                throw new BusinessException(phoneResult.responseMessage() == null ? "本机号一键登录失败" : phoneResult.responseMessage());
            }
            normalizedPhone = phoneNumberNormalizer.normalizeChinaPhone(phoneResult.phone());
            AccountEnsureResult accountResult = accountService.ensureAccount(
                PHONE_ACCOUNT_TYPE,
                AccountService.GLOBAL_CREDENTIAL_SCOPE,
                normalizedPhone
            );
            UserResolution resolution = resolveUserByPhoneAccount(accountResult.account(), normalizedPhone);
            AuthLoginResult result = createLoginResult(
                resolution.user(),
                accountResult.account(),
                context,
                accountResult.created(),
                resolution.created()
            );
            loginLogService.recordSuccess(
                resolution.user(),
                accountResult.account(),
                requireResultSession(result),
                normalizedPhone,
                normalizedPhone,
                LoginVerificationMode.CARRIER_ONE_CLICK,
                context
            );
            publishLoginSucceeded(
                resolution.user().getId(),
                accountResult.account().getId(),
                accountResult.account().getType(),
                loginSide(context),
                accountResult.created(),
                resolution.created()
            );
            return result;
        } catch (BusinessException ex) {
            loginLogService.recordFailure(
                PHONE_ACCOUNT_TYPE,
                normalizedPhone,
                normalizedPhone,
                LoginVerificationMode.CARRIER_ONE_CLICK,
                ex.getMessage(),
                context
            );
            throw ex;
        }
    }

    /**
     * 微信小程序直接登录。
     *
     * <p>只使用 wx.login code 换取 openId，不要求手机号；适合允许微信身份直接进入的应用。
     * 账号唯一性由 wechat_miniapp + appCode + openId 保证。</p>
     */
    @Override
    @Transactional
    public AuthLoginResult loginByWechatMiniapp(WechatMiniappLoginCommand command) {
        LoginRequestContext context = new LoginRequestContext(
            command.clientType(),
            command.appCode(),
            command.accessScope(),
            command.ip(),
            command.userAgent()
        );
        String openId = null;
        try {
            String safeAppCode = requireClientApp(context);
            openId = wechatMiniappClient.codeToSession(safeAppCode, command.code()).openId();
            String credentialScope = safeAppCode;
            AccountEnsureResult accountResult = accountService.ensureAccount(WECHAT_ACCOUNT_TYPE, credentialScope, openId);
            UserService.ResolveUserResult resolution = userService.findOrCreateByAccountId(accountResult.account().getId());
            AuthLoginResult result = createLoginResult(
                resolution.user(),
                accountResult.account(),
                context,
                accountResult.created(),
                resolution.created()
            );
            loginLogService.recordSuccess(
                resolution.user(),
                accountResult.account(),
                requireResultSession(result),
                openId,
                resolution.user().getPhone(),
                LoginVerificationMode.WECHAT_OPENID,
                context
            );
            publishLoginSucceeded(
                resolution.user().getId(),
                accountResult.account().getId(),
                accountResult.account().getType(),
                loginSide(context),
                accountResult.created(),
                resolution.created()
            );
            return result;
        } catch (BusinessException ex) {
            loginLogService.recordFailure(
                WECHAT_ACCOUNT_TYPE,
                openId,
                null,
                LoginVerificationMode.WECHAT_OPENID,
                ex.getMessage(),
                context
            );
            throw ex;
        }
    }

    /**
     * 微信小程序绑定手机号登录。
     *
     * <p>同时解析 openId 和微信手机号，创建或复用 wechat_miniapp account 与 phone/GLOBAL account，
     * 并把两个账号绑定到同一个 user；如果两个账号已经归属不同 user，则拒绝绑定。</p>
     */
    @Override
    @Transactional
    public AuthLoginResult loginByWechatMiniappPhone(WechatMiniappPhoneLoginCommand command) {
        LoginRequestContext context = new LoginRequestContext(
            command.clientType(),
            command.appCode(),
            command.accessScope(),
            command.ip(),
            command.userAgent()
        );
        String openId = null;
        String phone = null;
        try {
            String safeAppCode = requireClientApp(context);
            openId = wechatMiniappClient.codeToSession(safeAppCode, command.code()).openId();
            phone = phoneNumberNormalizer.normalizeChinaPhone(wechatMiniappClient.getPhoneNumber(safeAppCode, command.phoneCode()).phoneNumber());
            String credentialScope = safeAppCode;

            AccountEnsureResult phoneAccountResult = accountService.ensureAccount(
                PHONE_ACCOUNT_TYPE,
                AccountService.GLOBAL_CREDENTIAL_SCOPE,
                phone
            );
            AccountEnsureResult wechatAccountResult = accountService.ensureAccount(WECHAT_ACCOUNT_TYPE, credentialScope, openId);
            UserResolution resolution = resolveWechatPhoneUser(
                phoneAccountResult.account(),
                wechatAccountResult.account(),
                phone
            );

            AuthLoginResult result = createLoginResult(
                resolution.user(),
                wechatAccountResult.account(),
                context,
                wechatAccountResult.created() || phoneAccountResult.created(),
                resolution.created()
            );
            loginLogService.recordSuccess(
                resolution.user(),
                wechatAccountResult.account(),
                requireResultSession(result),
                openId,
                phone,
                LoginVerificationMode.WECHAT_PHONE,
                context
            );
            publishLoginSucceeded(
                resolution.user().getId(),
                wechatAccountResult.account().getId(),
                wechatAccountResult.account().getType(),
                loginSide(context),
                wechatAccountResult.created() || phoneAccountResult.created(),
                resolution.created()
            );
            return result;
        } catch (BusinessException ex) {
            loginLogService.recordFailure(
                WECHAT_ACCOUNT_TYPE,
                openId,
                phone,
                LoginVerificationMode.WECHAT_PHONE,
                ex.getMessage(),
                context
            );
            throw ex;
        }
    }

    /**
     * 使用 Refresh Token 换取新的 Access Token。
     *
     * <p>会校验 token 类型、会话状态、refresh jti 是否匹配，并确认用户仍然有效。</p>
     */
    @Override
    @Transactional
    public AuthRefreshResult refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException("Refresh Token 已过期或无效");
        }

        AuthSessionPO session = authSessionService.requireActiveBySessionNo(jwtTokenProvider.getSessionNoFromToken(refreshToken));
        if (!session.getRefreshTokenJti().equals(jwtTokenProvider.getTokenId(refreshToken))) {
            throw new BusinessException("Refresh Token 已失效");
        }
        if (!session.getClientType().equals(jwtTokenProvider.getClientTypeFromToken(refreshToken))
            || !session.getAppCode().equals(jwtTokenProvider.getAppCodeFromToken(refreshToken))
            || !session.getAccessScope().equals(jwtTokenProvider.getAccessScopeFromToken(refreshToken))) {
            throw new BusinessException("会话不匹配");
        }
        userService.getRequiredActiveById(session.getUserId());
        authSessionService.touch(session.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(
            session.getSessionNo(),
            session.getClientType(),
            session.getAppCode(),
            session.getAccessScope(),
            jwtTokenProvider.newTokenId()
        );
        return new AuthRefreshResult(
            accessToken,
            "Bearer",
            jwtTokenProvider.getAccessExpiresInSeconds(),
            session.getClientType(),
            session.getAppCode(),
            session.getAccessScope()
        );
    }

    /**
     * 退出登录。
     *
     * <p>通过吊销 auth_session 让该会话后续访问失效。</p>
     */
    @Override
    public void logout(Long sessionId) {
        authSessionService.revoke(sessionId);
    }

    /**
     * 校验登录请求里的基础技术上下文。
     *
     * <p>这里不做 appCode、clientType、accessScope 之间的绑定关系判断，
     * 只保证它们存在且枚举值合法。</p>
     */
    private String requireClientApp(LoginRequestContext context) {
        String safeAppCode = context.safeAppCode();
        context.safeClientType();
        context.safeAccessScope();
        return safeAppCode;
    }

    /**
     * 创建登录结果。
     *
     * <p>统一负责创建 auth_session、生成 accessToken 和 refreshToken；
     * 具体登录方式只需要先解析出 user、account 和请求上下文。</p>
     */
    private AuthLoginResult createLoginResult(
        UserPO user,
        AccountPO account,
        LoginRequestContext context,
        boolean accountCreated,
        boolean userCreated
    ) {
        loginAccessGuards.forEach(guard -> guard.check(user, context));
        String refreshJti = jwtTokenProvider.newTokenId();
        AuthSessionPO session = authSessionService.createSession(
            user,
            account,
            refreshJti,
            jwtTokenProvider.getRefreshExpiresAtFromNow(),
            context
        );
        String accessToken = jwtTokenProvider.generateAccessToken(
            session.getSessionNo(),
            session.getClientType(),
            session.getAppCode(),
            session.getAccessScope(),
            jwtTokenProvider.newTokenId()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
            session.getSessionNo(),
            session.getClientType(),
            session.getAppCode(),
            session.getAccessScope(),
            refreshJti
        );
        return new AuthLoginResult(
            accessToken,
            refreshToken,
            "Bearer",
            jwtTokenProvider.getAccessExpiresInSeconds(),
            jwtTokenProvider.getRefreshExpiresInSeconds(),
            session.getClientType(),
            session.getAppCode(),
            session.getAccessScope(),
            accountCreated || userCreated
        );
    }

    private AccountLoginSide loginSide(LoginRequestContext context) {
        return AccessScope.CLIENT.name().equals(context.safeAccessScope()) ? AccountLoginSide.CLIENT : AccountLoginSide.ADMIN;
    }

    private AuthSessionPO requireResultSession(AuthLoginResult result) {
        return authSessionService.requireActiveBySessionNo(jwtTokenProvider.getSessionNoFromToken(result.accessToken()));
    }

    /**
     * 根据手机号账号解析用户。
     *
     * <p>用于手机号验证码登录：先通过 phone account 找到或创建 user，再把手机号同步到 user.phone。</p>
     */
    private UserResolution resolveUserByPhoneAccount(AccountPO phoneAccount, String phone) {
        UserService.ResolveUserResult result = userService.findOrCreateByAccountId(phoneAccount.getId());
        UserPO user = userService.ensurePrimaryPhone(result.user().getId(), phone);
        return new UserResolution(user, result.created());
    }

    /**
     * 解析微信绑定手机号登录时最终应该使用哪个 user。
     *
     * <p>这里处理四种情况：两个账号已绑定同一 user、只有手机号账号有 user、只有微信账号有 user、
     * 两个账号都没有 user；如果两个账号分别属于不同 user，则拒绝绑定。</p>
     */
    private UserResolution resolveWechatPhoneUser(AccountPO phoneAccount, AccountPO wechatAccount, String phone) {
        Optional<UserPO> phoneUser = userService.findByAccountId(phoneAccount.getId());
        Optional<UserPO> wechatUser = userService.findByAccountId(wechatAccount.getId());

        if (phoneUser.isPresent() && wechatUser.isPresent()) {
            if (!phoneUser.get().getId().equals(wechatUser.get().getId())) {
                throw new BusinessException("微信账号已绑定其他手机号");
            }
            userService.getRequiredActiveById(phoneUser.get().getId());
            UserPO user = userService.ensurePrimaryPhone(phoneUser.get().getId(), phone);
            return new UserResolution(userService.getRequiredActiveById(user.getId()), false);
        }

        if (phoneUser.isPresent()) {
            userService.getRequiredActiveById(phoneUser.get().getId());
            UserPO user = userService.ensurePrimaryPhone(phoneUser.get().getId(), phone);
            bindAccountToUser(wechatAccount, user.getId());
            return new UserResolution(userService.getRequiredActiveById(user.getId()), false);
        }

        if (wechatUser.isPresent()) {
            userService.getRequiredActiveById(wechatUser.get().getId());
            UserPO user = userService.ensurePrimaryPhone(wechatUser.get().getId(), phone);
            bindAccountToUser(phoneAccount, user.getId());
            return new UserResolution(userService.getRequiredActiveById(user.getId()), false);
        }

        UserService.ResolveUserResult result = userService.findOrCreateByAccountId(phoneAccount.getId());
        UserPO user = userService.ensurePrimaryPhone(result.user().getId(), phone);
        bindAccountToUser(wechatAccount, user.getId());
        return new UserResolution(userService.getRequiredActiveById(user.getId()), result.created());
    }

    /**
     * 把账号绑定到指定用户。
     *
     * <p>如果账号已经绑定到该用户则直接返回；如果已绑定到其他用户则拒绝。
     * 捕获唯一键冲突是为了兼容并发绑定下另一个事务先写入的情况。</p>
     */
    private void bindAccountToUser(AccountPO account, Long userId) {
        bindAccountToUser(account, userId, "微信账号已绑定其他手机号");
    }

    private void bindAccountToUser(AccountPO account, Long userId, String conflictMessage) {
        UserAccountPO existing = userAccountRepository.findByAccountId(account.getId()).orElse(null);
        if (existing != null) {
            if (!existing.getUserId().equals(userId)) {
                throw new BusinessException(conflictMessage);
            }
            return;
        }

        try {
            UserAccountPO link = new UserAccountPO();
            link.setUserId(userId);
            link.setAccountId(account.getId());
            userAccountRepository.saveAndFlush(link);
        } catch (DataIntegrityViolationException ex) {
            UserAccountPO concurrent = userAccountRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException("账号绑定失败"));
            if (!concurrent.getUserId().equals(userId)) {
                throw new BusinessException(conflictMessage);
            }
        }
    }

    /**
     * 发布登录成功领域事件。
     *
     * <p>当前用于把账号登录结果通知给其他模块；调用方会传入账号和用户是否本次新建。</p>
     */
    private void publishLoginSucceeded(
        Long userId,
        Long accountId,
        String accountType,
        AccountLoginSide side,
        boolean accountCreated,
        boolean userCreated
    ) {
        eventPublisher.publishEvent(new AccountLoginSucceededEvent(
            userId,
            accountId,
            accountType,
            side,
            accountCreated,
            userCreated
        ));
    }

    private AccountPO getRequiredUserPasswordAccount(String phone) {
        AccountPO account = accountService.getByTypeAndCredentialScopeAndIdentifier(
            PHONE_PASSWORD_ACCOUNT_TYPE,
            AccountService.GLOBAL_CREDENTIAL_SCOPE,
            phone
        );
        if (account == null) {
            throw new BusinessException("手机号或密码错误");
        }
        return requireActive(account);
    }

    /**
     * 校验账号是否可用。
     *
     * <p>账号被禁用时统一抛出业务异常；正常账号原样返回，方便链式调用。</p>
     */
    private AccountPO requireActive(AccountPO account) {
        if (account.getStatus() == AccountPO.Status.DISABLED) {
            throw new BusinessException("账号已被禁用");
        }
        return account;
    }

    private record UserResolution(UserPO user, boolean created) {
    }
}
