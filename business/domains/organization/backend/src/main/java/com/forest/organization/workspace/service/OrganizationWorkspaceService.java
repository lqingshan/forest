package com.forest.organization.workspace.service;

/**
 * 企业工作台准入解析服务。
 *
 * <p>统一把 {@code organizationNo + userId} 解析成当前企业和当前员工身份。
 * 调用方不应该信任前端传来的企业编号；每次请求都必须通过本服务重新校验
 * 企业可用和员工 ACTIVE 状态。</p>
 */
public interface OrganizationWorkspaceService {
    /**
     * 解析并校验当前 user 是否可以进入指定企业工作台。
     */
    OrganizationWorkspaceState resolve(String organizationNo, Long userId);
}
