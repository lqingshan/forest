package com.forest.user.identity.provisioning;

import com.forest.starter.exception.BusinessException;
import com.forest.user.account.entity.AccountPO;
import com.forest.user.account.password.PasswordSecretCodec;
import com.forest.user.account.service.AccountEnsureResult;
import com.forest.user.account.service.AccountService;
import com.forest.user.auth.service.PhoneNumberNormalizer;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.repository.UserRepository;
import com.forest.user.user.service.UserService;
import com.forest.user.useraccount.entity.UserAccountPO;
import com.forest.user.useraccount.repository.UserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认用户身份准备实现。
 */
@Service
public class UserIdentityProvisioningServiceImpl implements UserIdentityProvisioningService {
    private static final String PHONE_ACCOUNT_TYPE = "phone";
    private static final String PHONE_PASSWORD_ACCOUNT_TYPE = "phone_password";
    private static final String DEFAULT_USER_NAME = "新用户";

    private final AccountService accountService;
    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PhoneNumberNormalizer phoneNumberNormalizer;
    private final PasswordSecretCodec passwordSecretCodec;

    public UserIdentityProvisioningServiceImpl(
        AccountService accountService,
        UserAccountRepository userAccountRepository,
        UserRepository userRepository,
        UserService userService,
        PhoneNumberNormalizer phoneNumberNormalizer,
        PasswordSecretCodec passwordSecretCodec
    ) {
        this.accountService = accountService;
        this.userAccountRepository = userAccountRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.phoneNumberNormalizer = phoneNumberNormalizer;
        this.passwordSecretCodec = passwordSecretCodec;
    }

    @Override
    @Transactional
    public ProvisionedIdentity provisionPhonePasswordIdentity(ProvisionPhonePasswordIdentityCommand command) {
        if (command == null) {
            throw new BusinessException("用户身份准备命令不能为空");
        }
        String normalizedPhone = phoneNumberNormalizer.normalizeChinaPhone(command.phone());
        AccountEnsureResult phoneAccount = accountService.ensureAccount(
            PHONE_ACCOUNT_TYPE,
            AccountService.GLOBAL_CREDENTIAL_SCOPE,
            normalizedPhone,
            null
        );
        UserService.ResolveUserResult resolution = userService.findOrCreateByAccountId(phoneAccount.account().getId());
        UserPO user = userService.ensurePrimaryPhone(resolution.user().getId(), normalizedPhone);
        user = applyNameIfNeeded(user, command.name());
        ensurePasswordAccount(user.getId(), normalizedPhone, command.initialPassword());
        return new ProvisionedIdentity(user.getId(), user.getName(), user.getPhone());
    }

    private UserPO applyNameIfNeeded(UserPO user, String name) {
        if (name == null || name.isBlank()) {
            return user;
        }
        if (user.getName() != null && !user.getName().isBlank() && !DEFAULT_USER_NAME.equals(user.getName())) {
            return user;
        }
        user.setName(name.trim());
        user.setModifiedId(user.getId());
        return userRepository.save(user);
    }

    private AccountPO ensurePasswordAccount(Long userId, String normalizedPhone, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException("初始密码不能为空");
        }
        AccountEnsureResult passwordAccount = accountService.ensureAccount(
            PHONE_PASSWORD_ACCOUNT_TYPE,
            AccountService.GLOBAL_CREDENTIAL_SCOPE,
            normalizedPhone,
            passwordSecretCodec.encode(rawPassword)
        );
        bindAccountToUser(passwordAccount.account(), userId, "手机号密码账号已绑定其他用户");
        return passwordAccount.account();
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
            link.setCreatedId(userId);
            link.setModifiedId(userId);
            userAccountRepository.saveAndFlush(link);
        } catch (DataIntegrityViolationException ex) {
            UserAccountPO concurrent = userAccountRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException("账号绑定失败"));
            if (!concurrent.getUserId().equals(userId)) {
                throw new BusinessException(conflictMessage);
            }
        }
    }

}
