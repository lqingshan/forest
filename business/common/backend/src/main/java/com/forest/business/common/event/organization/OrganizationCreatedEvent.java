package com.forest.business.common.event.organization;

/**
 * 表示企业创建成功这一跨业务事实。
 *
 * <p>organization 只发布该事件，不直接调用 access 初始化 RBAC；access 监听该事件后
 * 为新企业创建默认角色，并给初始 owner member 授予企业所有者角色。</p>
 */
public record OrganizationCreatedEvent(
    /**
     * 新企业 ID。
     */
    Long organizationId,

    /**
     * 企业初始 owner 员工身份 ID。
     */
    Long ownerMemberId,

    /**
     * 创建企业的 user ID，也是默认角色和授权关系的初始化审计人。
     */
    Long ownerUserId
) {
    /**
     * 创建企业创建事件。
     *
     * <p>调用方传入的是刚创建完成的企业 ID、初始 owner member ID，以及创建企业的 user ID。
     * 使用工厂方法可以让事件语义在调用点更清晰，也避免到处直接暴露 record 构造参数顺序。</p>
     */
    public static OrganizationCreatedEvent of(Long organizationId, Long ownerMemberId, Long ownerUserId) {
        return new OrganizationCreatedEvent(organizationId, ownerMemberId, ownerUserId);
    }
}
