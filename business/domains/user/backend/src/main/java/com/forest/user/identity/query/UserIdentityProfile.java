package com.forest.user.identity.query;

/**
 * 表示外部业务展示员工、操作者等自然人身份时需要的轻量资料。
 */
public record UserIdentityProfile(
    Long userId,
    String name,
    String phone
) {
}
