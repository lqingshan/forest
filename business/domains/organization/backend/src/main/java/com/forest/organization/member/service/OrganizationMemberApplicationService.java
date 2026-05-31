package com.forest.organization.member.service;

import com.forest.organization.member.entity.OrganizationMemberPO;

import java.util.List;

/**
 * 定义企业员工用例编排能力。
 *
 * <p>该接口面向 controller 和其他应用层调用者，负责把企业、部门、用户身份和员工关系串成完整用例。</p>
 */
public interface OrganizationMemberApplicationService {
    OrganizationMemberPO addMember(AddMemberCommand command);

    List<OrganizationMemberListItem> listMembers(String organizationNo, Long operatorUserId);

    OrganizationMemberPO updateMember(UpdateMemberCommand command);

    OrganizationMemberPO updateStatus(UpdateMemberStatusCommand command);

    record AddMemberCommand(
        String organizationNo,
        Long operatorUserId,
        String phone,
        String name,
        String initialPassword,
        Long departmentId,
        String roleCode
    ) {
    }

    record UpdateMemberCommand(
        String organizationNo,
        Long operatorUserId,
        Long memberId,
        Long departmentId,
        String roleCode
    ) {
    }

    record UpdateMemberStatusCommand(
        String organizationNo,
        Long memberId,
        OrganizationMemberPO.Status status,
        Long operatorUserId
    ) {
    }

    /**
     * 企业员工管理列表项。
     *
     * <p>这个返回模型只表达 organization domain 的员工基础信息，不携带 {@code userId}
     * 和 access 角色授权信息。角色查看和分配统一由 organization-access aggregation 提供。</p>
     */
    record OrganizationMemberListItem(
        Long memberId,
        String memberNo,
        String name,
        String phone,
        Long departmentId,
        OrganizationMemberPO.Status status
    ) {
    }
}
