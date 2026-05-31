package com.forest.access.core;

/**
 * RBAC 角色授权生效的业务边界类型。
 *
 * <p>权限主体回答“谁拥有角色”，边界回答“这个角色在哪个范围内生效”。
 * 同一个 {@code organization_member} 可以在不同边界下拥有不同角色。</p>
 *
 * <p>例如平台企业员工可以同时拥有：</p>
 * <ul>
 *     <li>{@code ORGANIZATION:88} 下的平台公司内部管理员角色，用来管理平台公司自己的组织架构。</li>
 *     <li>{@code PLATFORM:0} 下的平台治理角色，用来审核商家认证、修改商家状态。</li>
 * </ul>
 */
public enum AccessBoundaryType {
    /**
     * 企业边界。
     *
     * <p>{@code boundaryId} 是具体 {@code organization.id}，用于企业自管理能力，
     * 例如组织资料、部门、员工、角色权限等。</p>
     */
    ORGANIZATION,

    /**
     * 平台治理边界。
     *
     * <p>{@code boundaryId} 来自 {@code forest.platform.boundary-id}，默认是 {@code 0}，
     * 用于全平台治理能力，例如商家认证审核、企业状态管理等。</p>
     */
    PLATFORM
}
