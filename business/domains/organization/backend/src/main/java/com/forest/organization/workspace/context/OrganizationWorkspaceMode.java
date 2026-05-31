package com.forest.organization.workspace.context;

import com.forest.organization.core.entity.OrganizationPO;

/**
 * 企业工作台模式。
 *
 * <p>该枚举表达当前企业认证状态下，前端应该进入哪种工作台体验。
 * 后端接口仍然通过企业认证 Gate 和 RBAC 做真实拦截，不能只依赖该字段授权。</p>
 */
public enum OrganizationWorkspaceMode {
    /**
     * 完整企业工作台：企业认证已通过，可结合 RBAC 权限展示部门、员工、角色权限等完整后台功能。
     */
    FULL,

    /**
     * 认证受限工作台：企业尚未认证通过，只展示企业资料、认证提交和认证状态等受限功能。
     */
    CERTIFICATION_ONLY;

    public static OrganizationWorkspaceMode fromCertificationStatus(OrganizationPO.CertificationStatus certificationStatus) {
        return certificationStatus == OrganizationPO.CertificationStatus.APPROVED ? FULL : CERTIFICATION_ONLY;
    }
}
