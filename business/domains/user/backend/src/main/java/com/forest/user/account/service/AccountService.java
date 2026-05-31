package com.forest.user.account.service;

import com.forest.user.account.entity.AccountPO;

/**
 * 定义账号查询与创建能力。
 */
public interface AccountService {
    String GLOBAL_CREDENTIAL_SCOPE = AccountPO.GLOBAL_CREDENTIAL_SCOPE;

    AccountPO getRequiredById(Long accountId);

    AccountPO getByTypeAndIdentifier(String type, String identifier);

    AccountPO getByTypeAndCredentialScopeAndIdentifier(String type, String credentialScope, String identifier);

    AccountPO createAccount(String type, String identifier);

    AccountPO createAccount(String type, String credentialScope, String identifier);

    /**
     * 创建或复用 GLOBAL scope 下的账号，并返回本次是否新建。
     *
     * <p>该方法由 account 领域统一处理唯一键并发冲突：如果并发请求同时创建同一账号，
     * 最终只会有一个请求真正插入，其他请求重新查询已有账号并返回。</p>
     *
     * @param type 账号类型，例如 phone、phone_password、wechat_miniapp
     * @param identifier 账号标识，例如手机号、openid
     * @return 最终可用账号以及是否由本次调用新建
     */
    AccountEnsureResult ensureAccount(String type, String identifier);

    /**
     * 创建或复用指定 credentialScope 下的账号，并返回本次是否新建。
     *
     * <p>credentialScope 用来区分同一账号类型下的凭证命名空间：
     * 手机号、密码等全局账号通常使用 GLOBAL；微信小程序账号通常使用 appCode。</p>
     *
     * @param type 账号类型，例如 phone、phone_password、wechat_miniapp
     * @param credentialScope 凭证命名空间，空值会按 GLOBAL 处理
     * @param identifier 账号标识，例如手机号、openid
     * @return 最终可用账号以及是否由本次调用新建
     */
    AccountEnsureResult ensureAccount(String type, String credentialScope, String identifier);

    /**
     * 创建或复用指定 credentialScope 下的账号，并在新建账号时写入 secret。
     *
     * <p>该方法主要用于手机号密码账号等需要保存密文凭证的场景。
     * 如果账号已经存在，本方法只复用已有账号，不会覆盖已有 secret，避免管理员添加员工等流程误改旧密码。</p>
     *
     * @param type 账号类型，例如 phone_password
     * @param credentialScope 凭证命名空间，空值会按 GLOBAL 处理
     * @param identifier 账号标识，例如手机号
     * @param secret 新建账号时要写入的密文凭证，可为空
     * @return 最终可用账号以及是否由本次调用新建
     */
    AccountEnsureResult ensureAccount(String type, String credentialScope, String identifier, String secret);

    String getRequiredIdentifierByUserIdAndType(Long userId, String type);

    String getRequiredIdentifierByUserIdAndTypeAndCredentialScope(Long userId, String type, String credentialScope);
}
