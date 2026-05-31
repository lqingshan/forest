package com.forest.access.role.service;

public final class AccessRoleCodes {
    /**
     * 企业所有者角色。
     *
     * <p>企业创建时默认授予初始 owner member，通常拥有 {@code organization.*} 和
     * {@code access.*}。业务上需要保证每个企业至少保留一个有效所有者。</p>
     */
    public static final String ORGANIZATION_OWNER = "organization_owner";

    /**
     * 企业管理员角色。
     *
     * <p>用于企业日常管理，默认覆盖企业资料、认证提交、部门管理和员工管理等组织自管理能力。</p>
     */
    public static final String ORGANIZATION_ADMIN = "organization_admin";

    /**
     * 普通员工角色。
     *
     * <p>用于企业内基础成员身份，默认只授予企业信息、部门、员工等只读能力。</p>
     */
    public static final String ORGANIZATION_MEMBER = "organization_member";

    /**
     * 平台超级管理员角色。
     *
     * <p>作用于 {@code PLATFORM:{boundaryId}} 平台治理边界，不等同于平台企业自己的
     * {@code ORGANIZATION:{organizationId}} 管理员。</p>
     */
    public static final String PLATFORM_SUPER_ADMIN = "platform_super_admin";

    /**
     * 平台企业认证审核员角色。
     *
     * <p>作用于平台治理边界，面向企业认证申请查看、审核等 {@code platform.organization.certification.*}
     * 能力。</p>
     */
    public static final String PLATFORM_CERTIFICATION_REVIEWER = "platform_certification_reviewer";

    private AccessRoleCodes() {
    }
}
