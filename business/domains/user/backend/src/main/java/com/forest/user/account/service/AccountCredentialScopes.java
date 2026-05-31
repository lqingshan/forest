package com.forest.user.account.service;

import com.forest.user.account.entity.AccountPO;

/**
 * 定义账号凭证命名空间的通用规则。
 */
public final class AccountCredentialScopes {
    public static final String GLOBAL = AccountPO.GLOBAL_CREDENTIAL_SCOPE;

    private AccountCredentialScopes() {
    }

    /**
     * 规范化账号凭证命名空间。
     *
     * <p>空值统一回退为 GLOBAL，避免调用方忘传 scope 时产生 null 或空字符串命名空间。</p>
     */
    public static String normalize(String credentialScope) {
        if (credentialScope == null || credentialScope.isBlank()) {
            return GLOBAL;
        }
        return credentialScope.trim();
    }
}
