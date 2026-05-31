package com.forest.access.core;

/**
 * 一次 RBAC 权限检查所需的上下文。
 *
 * <p>它回答的是“用谁的身份、在哪个权限边界下做判断”，不保存具体权限点。
 * 具体权限点来自接口上的 {@code @RequirePermission} 等注解，再由权限检查逻辑结合本上下文
 * 查询角色授权关系。</p>
 *
 * <p>一期权限主体统一是 {@link AccessSubjectType#ORGANIZATION_MEMBER}，即企业员工身份。
 * 同一个 user 在不同企业会有不同的 organization member，因此权限不直接挂在 user 上。</p>
 *
 * <p>边界用于区分角色授权在哪个业务域内生效，例如：</p>
 * <ul>
 *     <li>{@code ORGANIZATION:88} 表示员工在企业 88 内做组织自管理权限判断。</li>
 *     <li>{@code PLATFORM:0} 表示平台企业员工在平台治理域内做平台权限判断。</li>
 * </ul>
 */
public record AccessCheckContext(
    /**
     * 权限主体类型，例如 {@code ORGANIZATION_MEMBER}。
     */
    AccessSubjectType subjectType,

    /**
     * 权限主体 ID，例如 {@code organization_member.id}。
     */
    Long subjectId,

    /**
     * 权限边界类型，例如企业边界 {@code ORGANIZATION} 或平台治理边界 {@code PLATFORM}。
     */
    AccessBoundaryType boundaryType,

    /**
     * 权限边界 ID，例如具体企业 ID，或平台治理默认边界 {@code 0}。
     */
    Long boundaryId
) {
    public static AccessCheckContext organizationMember(Long memberId, Long organizationId) {
        return new AccessCheckContext(
            AccessSubjectType.ORGANIZATION_MEMBER,
            memberId,
            AccessBoundaryType.ORGANIZATION,
            organizationId
        );
    }

    public static AccessCheckContext platformMember(Long memberId, Long platformBoundaryId) {
        return new AccessCheckContext(
            AccessSubjectType.ORGANIZATION_MEMBER,
            memberId,
            AccessBoundaryType.PLATFORM,
            platformBoundaryId
        );
    }
}
