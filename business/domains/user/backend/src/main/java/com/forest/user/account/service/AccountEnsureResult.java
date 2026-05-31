package com.forest.user.account.service;

import com.forest.user.account.entity.AccountPO;

/**
 * 表示账号创建或复用的结果。
 *
 * <p>{@code account} 是最终可用的账号；{@code created} 表示账号是否由本次调用新建。
 * 登录、身份准备等上层编排可以用 {@code created} 判断是否需要发布“新账号”相关事件，
 * 但不需要关心账号创建的唯一键并发处理细节。</p>
 */
public record AccountEnsureResult(
    /**
     * 最终可用账号。
     */
    AccountPO account,

    /**
     * 账号是否由本次调用新建。
     */
    boolean created
) {
}
