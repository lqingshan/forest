package com.forest.user.identity.provisioning;

/**
 * 提供用户身份准备能力。
 *
 * <p>该接口把 user、account、user_account 的编排收敛在 user 域内部，
 * 外部业务只需要说明“我要一个可登录的自然人身份”，不直接编排账号表和绑定表。</p>
 */
public interface UserIdentityProvisioningService {
    ProvisionedIdentity provisionPhonePasswordIdentity(ProvisionPhonePasswordIdentityCommand command);
}
