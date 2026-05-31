package com.forest.organizationaccess.admin.service.impl;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.core.AccessCheckContext;
import com.forest.access.core.AccessSubjectType;
import com.forest.access.permission.catalog.PermissionCatalog;
import com.forest.access.permission.registry.PermissionNode;
import com.forest.access.permission.registry.PermissionRegistry;
import com.forest.access.role.entity.AccessRolePO;
import com.forest.access.role.service.AccessControlService;
import com.forest.access.role.service.AccessRoleAssignmentService;
import com.forest.access.role.service.AccessRoleCodes;
import com.forest.access.role.service.AccessRoleManagementService;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.member.service.OrganizationMemberApplicationService;
import com.forest.organization.member.service.OrganizationMemberService;
import com.forest.organization.workspace.context.CurrentOrganizationWorkspace;
import com.forest.organization.workspace.context.OrganizationWorkspaceContext;
import com.forest.organizationaccess.admin.service.OrganizationAccessAdminService;
import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 默认企业工作台角色权限聚合服务。
 *
 * <p>这个类位于 organization-access aggregation，而不是 access domain，原因是这里的接口
 * 需要同时依赖“当前企业工作台上下文”和“RBAC 角色权限能力”。access domain 只负责角色、
 * 权限模式、授权关系等纯 RBAC 规则；当前企业是谁、当前员工是谁、企业认证 Gate 是否已通过，
 * 都由 organization workspace 提供。</p>
 *
 * <p>本类的主要编排动作是：从 {@link CurrentOrganizationWorkspace} 读取当前企业上下文，
 * 转换成 access domain 可理解的 {@link AccessCheckContext}，再调用 access 领域服务完成
 * 角色、权限和员工授权操作。</p>
 */
@Service
public class OrganizationAccessAdminServiceImpl implements OrganizationAccessAdminService {
    /**
     * 企业工作台角色授权只允许使用企业自管理和角色权限目录。
     *
     * <p>平台治理权限 {@code PLATFORM_GOVERNANCE} 不应该出现在商家工作台角色授权页，
     * 也不允许通过企业角色授予。</p>
     */
    private static final Set<PermissionCatalog> ORGANIZATION_ACCESS_CATALOGS = EnumSet.of(
        PermissionCatalog.ORGANIZATION_SELF,
        PermissionCatalog.ORGANIZATION_ACCESS
    );

    private final CurrentOrganizationWorkspace currentWorkspace;
    private final AccessControlService accessControlService;
    private final AccessRoleManagementService roleManagementService;
    private final AccessRoleAssignmentService roleAssignmentService;
    private final PermissionRegistry permissionRegistry;
    private final OrganizationMemberService memberService;
    private final OrganizationMemberApplicationService memberApplicationService;

    /**
     * 注入企业工作台上下文读取器、access 领域服务和 organization 员工服务。
     *
     * <p>构造器只完成依赖装配，不做上下文解析；当前企业和当前员工必须在具体请求方法中
     * 从 {@link CurrentOrganizationWorkspace} 读取。</p>
     */
    public OrganizationAccessAdminServiceImpl(
        CurrentOrganizationWorkspace currentWorkspace,
        AccessControlService accessControlService,
        AccessRoleManagementService roleManagementService,
        AccessRoleAssignmentService roleAssignmentService,
        PermissionRegistry permissionRegistry,
        OrganizationMemberService memberService,
        OrganizationMemberApplicationService memberApplicationService
    ) {
        this.currentWorkspace = currentWorkspace;
        this.accessControlService = accessControlService;
        this.roleManagementService = roleManagementService;
        this.roleAssignmentService = roleAssignmentService;
        this.permissionRegistry = permissionRegistry;
        this.memberService = memberService;
        this.memberApplicationService = memberApplicationService;
    }

    /**
     * 查询当前员工在当前企业工作台下的权限快照。
     *
     * <p>该方法读取当前工作台上下文，将 {@code memberId + organizationId} 转换成
     * access 领域的权限检查上下文，再查询已展开的精确权限点。返回结果只用于前端展示控制，
     * 后端真实权限仍由 {@code @RequirePermission} 保护。</p>
     */
    @Override
    @Transactional(readOnly = true)
    public MyPermissionsVO myPermissions() {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        Set<String> permissions = accessControlService.listPermissionCodes(workspace.toOrganizationAccessContext());
        return new MyPermissionsVO(
            workspace.organizationNo(),
            workspace.workspaceMode(),
            workspace.certified(),
            permissions.stream().sorted().toList()
        );
    }

    /**
     * 查询企业工作台角色授权页可展示的权限树。
     *
     * <p>这里只返回企业自管理和角色权限目录，不返回平台治理目录，避免商家企业角色被授予
     * 平台级权限。</p>
     */
    @Override
    public List<PermissionNode> permissionTree() {
        return permissionRegistry.tree(ORGANIZATION_ACCESS_CATALOGS);
    }

    /**
     * 查询当前企业边界下的角色列表。
     *
     * <p>角色按 {@code ORGANIZATION:{organizationId}} 隔离，因此同名角色在不同企业中
     * 是不同数据。</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleVO> listRoles() {
        AccessCheckContext context = currentWorkspace.require().toOrganizationAccessContext();
        return roleManagementService.listRoles(context.boundaryType(), context.boundaryId()).stream()
            .map(this::toRoleVO)
            .toList();
    }

    /**
     * 查询当前企业边界下的单个角色详情。
     *
     * <p>access 领域服务会校验 {@code roleId} 是否属于当前边界，避免跨企业读取角色。</p>
     */
    @Override
    @Transactional(readOnly = true)
    public RoleVO getRole(Long roleId) {
        AccessCheckContext context = currentWorkspace.require().toOrganizationAccessContext();
        return toRoleVO(roleManagementService.requireRole(roleId, context.boundaryType(), context.boundaryId()));
    }

    /**
     * 在当前企业边界下创建自定义角色。
     *
     * <p>角色 code 由 access 领域服务生成；本方法只负责提供当前企业边界、创建人和允许授权的
     * 权限目录。</p>
     */
    @Override
    public RoleVO createRole(String roleName, List<String> permissionPatterns) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        AccessCheckContext context = workspace.toOrganizationAccessContext();
        return toRoleVO(roleManagementService.createRole(
            context.boundaryType(),
            context.boundaryId(),
            roleName,
            permissionPatterns,
            workspace.userId(),
            ORGANIZATION_ACCESS_CATALOGS
        ));
    }

    /**
     * 更新当前企业边界下的角色基础信息。
     *
     * <p>是否允许修改系统预设角色、角色状态是否合法等规则由 access 领域服务负责。</p>
     */
    @Override
    public RoleVO updateRole(Long roleId, String roleName, AccessRolePO.Status status) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        AccessCheckContext context = workspace.toOrganizationAccessContext();
        return toRoleVO(roleManagementService.updateRole(
            roleId,
            context.boundaryType(),
            context.boundaryId(),
            roleName,
            status,
            workspace.userId()
        ));
    }

    /**
     * 替换当前企业边界下某个角色的权限模式集合。
     *
     * <p>权限模式会被限制在企业可授权目录内；新增、软删除、恢复软删除记录等集合替换细节
     * 由 access 领域服务处理。</p>
     */
    @Override
    public RoleVO replaceRolePermissions(Long roleId, List<String> permissionPatterns) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        AccessCheckContext context = workspace.toOrganizationAccessContext();
        return toRoleVO(roleManagementService.replacePermissions(
            roleId,
            context.boundaryType(),
            context.boundaryId(),
            permissionPatterns,
            workspace.userId(),
            ORGANIZATION_ACCESS_CATALOGS
        ));
    }

    /**
     * 删除当前企业边界下的自定义角色。
     *
     * <p>是否允许删除、是否仍存在有效授权关系等规则由 access 领域服务校验。</p>
     */
    @Override
    public void deleteRole(Long roleId) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        AccessCheckContext context = workspace.toOrganizationAccessContext();
        roleManagementService.deleteRole(roleId, context.boundaryType(), context.boundaryId(), workspace.userId());
    }

    /**
     * 查询角色授权页面需要的员工授权主体列表。
     *
     * <p>organization 员工用例只返回员工基础信息；本方法再按当前企业 RBAC 边界补齐
     * 每个员工已分配的角色，形成 access 授权页面专用视图。</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<AssignmentMemberVO> listMembers() {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        return memberApplicationService.listMembers(workspace.organizationNo(), workspace.userId()).stream()
            .map(member -> assignmentMemberVO(member, workspace.organizationId()))
            .toList();
    }

    /**
     * 查询指定员工在当前企业边界下已分配的角色。
     *
     * <p>先通过 organization domain 校验 {@code memberId} 属于当前企业，再按
     * {@code ORGANIZATION:{organizationId}} 边界查询角色分配。</p>
     */
    @Override
    @Transactional(readOnly = true)
    public MemberRolesVO listMemberRoles(Long memberId) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        memberService.requireMemberById(workspace.organizationId(), memberId);
        return memberRolesVO(memberId, roleAssignmentService.listAssignedRoles(
            AccessSubjectType.ORGANIZATION_MEMBER,
            memberId,
            AccessBoundaryType.ORGANIZATION,
            workspace.organizationId()
        ));
    }

    /**
     * 替换指定员工在当前企业边界下的角色集合。
     *
     * <p>该方法是典型聚合编排：organization domain 负责确认员工归属和状态，
     * 本类负责最后一个企业所有者保护，access domain 负责实际角色授权替换。</p>
     */
    @Override
    public MemberRolesVO replaceMemberRoles(Long memberId, List<Long> roleIds) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        OrganizationMemberPO member = memberService.requireMemberById(workspace.organizationId(), memberId);
        ensureNotRemovingLastActiveOwner(workspace.organizationId(), member, roleIds);
        return memberRolesVO(memberId, roleAssignmentService.replaceAssignedRoles(
            AccessSubjectType.ORGANIZATION_MEMBER,
            memberId,
            AccessBoundaryType.ORGANIZATION,
            workspace.organizationId(),
            roleIds,
            workspace.userId()
        ));
    }

    /**
     * 防止把最后一个 ACTIVE 企业所有者移除。
     *
     * <p>这是企业工作台层面的保护规则：access domain 不知道“ACTIVE 员工”这个组织状态，
     * 所以放在 aggregation 中结合员工状态和 RBAC 角色一起判断。</p>
     */
    private void ensureNotRemovingLastActiveOwner(Long organizationId, OrganizationMemberPO member, List<Long> requestedRoleIds) {
        if (member.getStatus() != OrganizationMemberPO.Status.ACTIVE
            || !accessControlService.hasOrganizationRole(organizationId, member.getId(), AccessRoleCodes.ORGANIZATION_OWNER)
            || requestedContainsOwnerRole(organizationId, requestedRoleIds)) {
            return;
        }
        long otherActiveOwnerCount = memberService.listByOrganization(organizationId).stream()
            .filter(item -> item.getStatus() == OrganizationMemberPO.Status.ACTIVE)
            .filter(item -> !item.getId().equals(member.getId()))
            .filter(item -> accessControlService.hasOrganizationRole(organizationId, item.getId(), AccessRoleCodes.ORGANIZATION_OWNER))
            .count();
        if (otherActiveOwnerCount <= 0) {
            throw BusinessException.of("至少保留一个企业所有者");
        }
    }

    /**
     * 判断本次提交的角色集合是否仍包含企业所有者角色。
     */
    private boolean requestedContainsOwnerRole(Long organizationId, List<Long> requestedRoleIds) {
        if (requestedRoleIds == null || requestedRoleIds.isEmpty()) {
            return false;
        }
        return roleManagementService.listRoles(AccessBoundaryType.ORGANIZATION, organizationId).stream()
            .filter(role -> AccessRoleCodes.ORGANIZATION_OWNER.equals(role.roleCode()))
            .anyMatch(role -> requestedRoleIds.contains(role.id()));
    }

    /**
     * 将 access domain 的角色详情转换成当前接口返回对象。
     */
    private RoleVO toRoleVO(AccessRoleManagementService.RoleDetail role) {
        return new RoleVO(
            role.id(),
            role.roleCode(),
            role.roleName(),
            role.systemPreset(),
            role.status(),
            role.permissionPatterns(),
            role.assignmentCount()
        );
    }

    /**
     * 将 organization 的员工基础列表项补充成 access 授权页面使用的员工视图。
     */
    private AssignmentMemberVO assignmentMemberVO(
        OrganizationMemberApplicationService.OrganizationMemberListItem member,
        Long organizationId
    ) {
        return new AssignmentMemberVO(
            member.memberId(),
            member.name(),
            member.phone(),
            member.departmentId(),
            member.status(),
            assignedRoleVOs(roleAssignmentService.listAssignedRoles(
                AccessSubjectType.ORGANIZATION_MEMBER,
                member.memberId(),
                AccessBoundaryType.ORGANIZATION,
                organizationId
            ))
        );
    }

    /**
     * 将 access domain 的授权查询结果转换成员工角色视图。
     */
    private MemberRolesVO memberRolesVO(Long memberId, List<AccessRoleAssignmentService.AssignedRole> assignedRoles) {
        List<AssignedRoleVO> roles = assignedRoleVOs(assignedRoles);
        return new MemberRolesVO(
            memberId,
            roles.stream().map(AssignedRoleVO::id).toList(),
            roles
        );
    }

    /**
     * 将 access domain 的已分配角色记录转换成接口返回对象。
     */
    private List<AssignedRoleVO> assignedRoleVOs(List<AccessRoleAssignmentService.AssignedRole> assignedRoles) {
        return assignedRoles.stream()
            .map(role -> new AssignedRoleVO(role.id(), role.roleCode(), role.roleName()))
            .toList();
    }
}
