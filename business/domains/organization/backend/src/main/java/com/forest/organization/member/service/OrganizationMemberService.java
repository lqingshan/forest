package com.forest.organization.member.service;

import com.forest.organization.member.entity.OrganizationMemberPO;

import java.util.List;

/**
 * 定义企业员工领域能力。
 *
 * <p>该接口只处理 organization_member 自身规则，不创建 user，不创建 account，
 * 也不拼装员工展示资料。跨 user/account/department 的用例编排由
 * {@link OrganizationMemberApplicationService} 负责。</p>
 */
public interface OrganizationMemberService {
    /**
     * 创建企业初始员工关系。
     *
     * <p>用于创建企业流程中的初始化步骤：企业和默认根部门已经创建完成后，
     * 将创建企业的 user 加入默认部门。企业所有者角色由 access 模块初始化，
     * 该方法不校验管理权限，也不创建 user/account。</p>
     */
    OrganizationMemberPO createOwner(Long organizationId, Long userId, Long departmentId);

    /**
     * 创建普通企业员工关系。
     *
     * <p>调用方必须先完成企业、部门、用户身份等用例编排。该方法只负责
     * organization_member 记录创建、同企业同用户重复加入校验和审计字段维护。</p>
     */
    OrganizationMemberPO createMember(CreateMemberCommand command);

    /**
     * 查询指定 user 当前加入的有效企业成员关系。
     *
     * <p>只返回未删除且状态为 ACTIVE 的成员关系，用于“我的企业列表”和登录后企业选择等场景。</p>
     */
    List<OrganizationMemberPO> listByUser(Long userId);

    /**
     * 查询指定企业下的全部未删除成员关系。
     *
     * <p>该方法保留 ACTIVE / DISABLED 等不同状态，便于员工管理列表展示和后台管理。</p>
     */
    List<OrganizationMemberPO> listByOrganization(Long organizationId);

    /**
     * 要求指定 user 是企业有效成员，并返回成员记录。
     *
     * <p>如果成员关系不存在或已停用，会抛出业务异常。用于企业详情、部门列表、
     * 企业认证等只要求“属于该企业”的场景。</p>
     */
    OrganizationMemberPO requireMember(Long organizationId, Long userId);

    /**
     * 要求指定 member 属于当前企业，并返回成员记录。
     *
     * <p>该方法只校验归属和未删除，不要求成员状态为 ACTIVE。员工角色分配页面需要能查看
     * DISABLED 员工的历史角色，但该员工身份在登录和权限判断时仍不会生效。</p>
     */
    OrganizationMemberPO requireMemberById(Long organizationId, Long memberId);

    /**
     * 按企业编号要求指定 user 是企业有效成员。
     *
     * <p>这是基于 organizationNo 的便捷校验方法，会先解析企业，再复用
     * {@link #requireMember(Long, Long)} 完成员工身份校验。</p>
     */
    void requireMemberByOrganizationNo(String organizationNo, Long userId);

    /**
     * 更新企业员工的部门。
     *
     * <p>该方法只修改 organization_member 自身字段，不移动部门树、不创建部门、
     * 不修改 user/account。角色授权由 access 模块维护。</p>
     */
    OrganizationMemberPO updateMember(UpdateMemberCommand command);

    /**
     * 更新企业员工状态。
     *
     * <p>用于启用或停用员工。停用企业所有者时会校验企业至少还保留一个有效所有者，
     * 防止企业失去最高管理员。</p>
     */
    OrganizationMemberPO updateStatus(UpdateMemberStatusCommand command);

    /**
     * 判断指定部门下是否存在未删除员工关系。
     *
     * <p>主要给部门删除流程使用：部门下仍有员工时，部门领域服务应拒绝删除。</p>
     */
    boolean existsByDepartment(Long departmentId);

    record CreateMemberCommand(
        Long organizationId,
        Long userId,
        Long departmentId,
        Long operatorUserId
    ) {
    }

    record UpdateMemberCommand(
        Long organizationId,
        Long memberId,
        Long departmentId,
        Long operatorUserId
    ) {
    }

    record UpdateMemberStatusCommand(
        Long organizationId,
        Long memberId,
        OrganizationMemberPO.Status status,
        Long operatorUserId
    ) {
    }
}
