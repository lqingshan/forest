package com.forest.user.account.service.impl;

import com.forest.starter.exception.BusinessException;
import com.forest.user.account.entity.AccountPO;
import com.forest.user.account.repository.AccountRepository;
import com.forest.user.account.service.AccountCredentialScopes;
import com.forest.user.account.service.AccountEnsureResult;
import com.forest.user.account.service.AccountService;
import com.forest.user.useraccount.repository.UserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 实现账号凭证的基础查询与创建能力。
 *
 * <p>本服务只处理 account 本体，不承载登录主流程，也不创建 user 或写入
 * user_account 以外的业务编排。登录能力统一收口在 {@code com.forest.user.auth}。</p>
 */
@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserAccountRepository userAccountRepository;
    private final TransactionTemplate requiresNewTransaction;

    public AccountServiceImpl(
        AccountRepository accountRepository,
        UserAccountRepository userAccountRepository,
        PlatformTransactionManager transactionManager
    ) {
        this.accountRepository = accountRepository;
        this.userAccountRepository = userAccountRepository;
        this.requiresNewTransaction = new TransactionTemplate(transactionManager);
        this.requiresNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * 根据账号 ID 获取可用账号；账号不存在或已禁用时抛出业务异常。
     */
    @Override
    public AccountPO getRequiredById(Long accountId) {
        AccountPO account = accountRepository.findById(accountId)
            .orElseThrow(() -> new BusinessException("账号不存在"));
        return requireActive(account);
    }

    /**
     * 按账号类型和标识查询全局凭证账号。
     */
    @Override
    public AccountPO getByTypeAndIdentifier(String type, String identifier) {
        return getByTypeAndCredentialScopeAndIdentifier(type, AccountService.GLOBAL_CREDENTIAL_SCOPE, identifier);
    }

    /**
     * 按账号类型、凭证命名空间和标识查询账号。
     */
    @Override
    public AccountPO getByTypeAndCredentialScopeAndIdentifier(String type, String credentialScope, String identifier) {
        return accountRepository.findByTypeAndCredentialScopeAndIdentifier(
            type,
            AccountCredentialScopes.normalize(credentialScope),
            identifier
        ).orElse(null);
    }

    /**
     * 创建或复用 GLOBAL scope 下的账号。
     */
    @Override
    public AccountPO createAccount(String type, String identifier) {
        return createAccount(type, AccountService.GLOBAL_CREDENTIAL_SCOPE, identifier);
    }

    /**
     * 创建或复用指定 credentialScope 下的账号。
     */
    @Override
    public AccountPO createAccount(String type, String credentialScope, String identifier) {
        return ensureAccount(type, credentialScope, identifier).account();
    }

    /**
     * 创建或复用 GLOBAL scope 下的账号，并返回是否新建。
     */
    @Override
    public AccountEnsureResult ensureAccount(String type, String identifier) {
        return ensureAccount(type, AccountService.GLOBAL_CREDENTIAL_SCOPE, identifier);
    }

    /**
     * 创建或复用指定 credentialScope 下的账号，并返回是否新建。
     */
    @Override
    public AccountEnsureResult ensureAccount(String type, String credentialScope, String identifier) {
        return ensureAccount(type, credentialScope, identifier, null);
    }

    /**
     * 创建或复用指定 credentialScope 下的账号，并在新建时写入 secret。
     *
     * <p>如果账号已存在，本方法不会覆盖原有 secret。</p>
     */
    @Override
    public AccountEnsureResult ensureAccount(String type, String credentialScope, String identifier, String secret) {
        return createAccountIfAbsent(type, credentialScope, identifier, secret);
    }

    /**
     * 查询某个用户在 GLOBAL scope 下绑定的指定类型账号标识。
     */
    @Override
    @Transactional(readOnly = true)
    public String getRequiredIdentifierByUserIdAndType(Long userId, String type) {
        return userAccountRepository.findByUserId(userId).stream()
            .map(link -> getRequiredById(link.getAccountId()))
            .filter(account -> type.equals(account.getType()))
            .map(AccountPO::getIdentifier)
            .findFirst()
            .orElseThrow(() -> new BusinessException("账号不存在"));
    }

    /**
     * 查询某个用户在指定 credentialScope 下绑定的指定类型账号标识。
     */
    @Override
    @Transactional(readOnly = true)
    public String getRequiredIdentifierByUserIdAndTypeAndCredentialScope(Long userId, String type, String credentialScope) {
        String safeScope = AccountCredentialScopes.normalize(credentialScope);
        return userAccountRepository.findByUserId(userId).stream()
            .map(link -> getRequiredById(link.getAccountId()))
            .filter(account -> type.equals(account.getType()))
            .filter(account -> safeScope.equals(account.getCredentialScope()))
            .map(AccountPO::getIdentifier)
            .findFirst()
            .orElseThrow(() -> new BusinessException("账号不存在"));
    }

    /**
     * 并发安全地创建或复用账号。
     *
     * <p>账号唯一性由 {@code type + credentialScope + identifier} 保证。
     * 本方法先查询已有账号；不存在时再尝试创建；创建完成后重新查询最终账号。
     * 这样上层的 auth / identity 编排只需要调用 {@link #ensureAccount(String, String, String, String)}，
     * 不需要各自复制唯一键冲突处理逻辑。</p>
     */
    private AccountEnsureResult createAccountIfAbsent(String type, String credentialScope, String identifier, String secret) {
        String safeScope = AccountCredentialScopes.normalize(credentialScope);
        AccountPO account = accountRepository.findByTypeAndCredentialScopeAndIdentifier(type, safeScope, identifier).orElse(null);
        if (account != null) {
            return new AccountEnsureResult(requireActive(account), false);
        }

        boolean created = tryCreateAccountInNewTransaction(type, safeScope, identifier, secret);
        account = accountRepository.findByTypeAndCredentialScopeAndIdentifier(type, safeScope, identifier)
            .orElseThrow(() -> new BusinessException("账号创建失败"));
        return new AccountEnsureResult(requireActive(account), created);
    }

    /**
     * 使用独立事务尝试插入账号。
     *
     * <p>如果并发请求已经先插入同一账号，数据库唯一键会触发
     * {@link DataIntegrityViolationException}。这里把它视为“本次没有创建成功”，
     * 交给外层重新查询已有账号。</p>
     */
    private boolean tryCreateAccountInNewTransaction(String type, String credentialScope, String identifier, String secret) {
        try {
            return Boolean.TRUE.equals(requiresNewTransaction.execute(status -> {
                AccountPO account = new AccountPO();
                account.setType(type);
                account.setCredentialScope(credentialScope);
                account.setIdentifier(identifier);
                account.setSecret(secret);
                account.setStatus(AccountPO.Status.ACTIVE);
                accountRepository.saveAndFlush(account);
                return true;
            }));
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    /**
     * 校验账号状态可用。
     */
    private AccountPO requireActive(AccountPO account) {
        if (account.getStatus() == AccountPO.Status.DISABLED) {
            throw new BusinessException("账号已被禁用");
        }
        return account;
    }

}
