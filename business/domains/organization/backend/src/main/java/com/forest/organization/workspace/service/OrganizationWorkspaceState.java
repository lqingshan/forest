package com.forest.organization.workspace.service;

import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.workspace.context.OrganizationWorkspaceContext;
import com.forest.organization.workspace.context.OrganizationWorkspaceMode;

/**
 * 企业工作台准入解析结果。
 *
 * <p>包含当前企业和当前员工身份，可用于 enter 接口返回工作台状态，
 * 也可转换成当前请求 ThreadLocal 使用的 {@link OrganizationWorkspaceContext}。</p>
 */
public record OrganizationWorkspaceState(
    OrganizationPO organization,
    OrganizationMemberPO member
) {
    public static OrganizationWorkspaceState of(OrganizationPO organization, OrganizationMemberPO member) {
        return new OrganizationWorkspaceState(organization, member);
    }

    public OrganizationWorkspaceContext toContext() {
        return new OrganizationWorkspaceContext(
            organization.getId(),
            organization.getOrganizationNo(),
            member.getId(),
            member.getUserId(),
            organization.getCertificationStatus()
        );
    }

    public boolean certified() {
        return organization.getCertificationStatus() == OrganizationPO.CertificationStatus.APPROVED;
    }

    public OrganizationWorkspaceMode workspaceMode() {
        return OrganizationWorkspaceMode.fromCertificationStatus(organization.getCertificationStatus());
    }
}
