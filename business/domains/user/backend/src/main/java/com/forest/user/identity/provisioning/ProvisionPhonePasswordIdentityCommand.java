package com.forest.user.identity.provisioning;

/**
 * 表示为外部业务准备“手机号 + 密码”自然人身份的命令。
 */
public record ProvisionPhonePasswordIdentityCommand(
    String phone,
    String name,
    String initialPassword
) {
}
