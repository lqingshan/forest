package com.forest.business.common.event.account;

/**
 * 表示账号登录成功这一跨业务事实。
 */
public record AccountLoginSucceededEvent(
    Long userId,
    Long accountId,
    String accountType,
    AccountLoginSide side,
    boolean accountCreated,
    boolean userCreated
) {
}
