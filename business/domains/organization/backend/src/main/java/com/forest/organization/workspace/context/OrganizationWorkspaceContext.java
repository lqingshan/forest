package com.forest.organization.workspace.context;

import com.forest.access.core.AccessCheckContext;
import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.workspace.web.OrganizationWorkspaceInterceptor;

/**
 * 当前请求中的企业工作台上下文。
 *
 * <p>它只存在于后端当前请求线程内，由 {@link OrganizationWorkspaceInterceptor}
 * 根据普通 ADMIN token 中的 user 身份和请求头 {@code X-Organization-No} 解析得到。
 * JWT、auth_session 和通用认证上下文都不保存这些企业信息。</p>
 */
public record OrganizationWorkspaceContext(
    /**
     * 当前工作台所属企业 ID。
     */
    Long organizationId,

    /**
     * 当前工作台所属企业编号。
     */
    String organizationNo,

    /**
     * 当前 user 在该企业内的员工身份 ID。
     */
    Long memberId,

    /**
     * 当前登录 user ID。
     */
    Long userId,

    /**
     * 当前企业认证状态，供企业认证 Gate 判断功能是否可用。
     */
    OrganizationPO.CertificationStatus certificationStatus
) {
    public boolean certified() {
        return certificationStatus == OrganizationPO.CertificationStatus.APPROVED;
    }

    public OrganizationWorkspaceMode workspaceMode() {
        return OrganizationWorkspaceMode.fromCertificationStatus(certificationStatus);
    }

    public AccessCheckContext toOrganizationAccessContext() {
        return AccessCheckContext.organizationMember(memberId, organizationId);
    }
}
