package com.forest.access.permission.catalog;

/**
 * 权限目录用于区分权限点所属的业务授权场景。
 *
 * <p>它只影响权限配置页的分组和授权时的理解边界，不参与接口路径判断，
 * 也不替代 RBAC 的 boundaryType / boundaryId 生效范围。</p>
 */
public enum PermissionCatalog {
    /**
     * 企业自管理权限。
     *
     * <p>用于企业维护自己的资料、部门、员工等组织能力。</p>
     */
    ORGANIZATION_SELF,

    /**
     * 企业角色权限管理。
     *
     * <p>用于企业在自己的组织边界内维护角色、授权和权限分配。</p>
     */
    ORGANIZATION_ACCESS,

    /**
     * 平台治理权限。
     *
     * <p>用于平台公司员工处理跨企业的平台后台能力，例如企业审核、状态治理等。</p>
     */
    PLATFORM_GOVERNANCE
}
