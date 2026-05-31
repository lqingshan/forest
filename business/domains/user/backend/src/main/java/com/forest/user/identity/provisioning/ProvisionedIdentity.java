package com.forest.user.identity.provisioning;

/**
 * 表示已经准备好的可登录自然人身份。
 */
public record ProvisionedIdentity(
    Long userId,
    String name,
    String phone
) {
}
