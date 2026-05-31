package com.forest.organization.core.service;

import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.workspace.service.OrganizationWorkspaceState;

import java.util.List;

/**
 * 企业入口用例服务。
 *
 * <p>这里处理“还没有进入某个企业工作台之前”的企业入口能力，例如创建企业、
 * 查询我的企业列表、选择并进入某个企业。该服务不依赖 {@code X-Organization-No}
 * 构建出来的工作台上下文。</p>
 */
public interface OrganizationEntryApplicationService {
    /**
     * 创建企业并初始化企业基础组织结构。
     *
     * <p>创建成功后，传入的 ownerUserId 会成为该企业的初始员工，并通过 RBAC 获得企业所有者角色。</p>
     */
    OrganizationPO createOrganization(String organizationName, Long ownerUserId);

    /**
     * 查询当前用户加入的有效企业列表。
     */
    List<OrganizationPO> listMyOrganizations(Long userId);

    /**
     * 选择并进入指定企业。
     *
     * <p>用于前端在企业入口阶段确认当前 user 能否进入该企业，并返回工作台状态。
     * 该方法不签发 workspace token，也不写入请求上下文。</p>
     */
    OrganizationWorkspaceState enterWorkspace(String organizationNo, Long userId);
}
