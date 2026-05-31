package com.forest.organizationaccess.admin.service;

import com.forest.access.permission.registry.PermissionNode;
import com.forest.access.role.entity.AccessRolePO;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.workspace.context.OrganizationWorkspaceMode;

import java.util.List;

/**
 * 企业工作台角色权限聚合用例。
 *
 * <p>该服务编排 organization 工作台上下文与 access RBAC 能力，不拥有权限点或企业主数据。</p>
 */
public interface OrganizationAccessAdminService {
    MyPermissionsVO myPermissions();

    List<PermissionNode> permissionTree();

    List<RoleVO> listRoles();

    RoleVO getRole(Long roleId);

    RoleVO createRole(String roleName, List<String> permissionPatterns);

    RoleVO updateRole(Long roleId, String roleName, AccessRolePO.Status status);

    RoleVO replaceRolePermissions(Long roleId, List<String> permissionPatterns);

    void deleteRole(Long roleId);

    List<AssignmentMemberVO> listMembers();

    MemberRolesVO listMemberRoles(Long memberId);

    MemberRolesVO replaceMemberRoles(Long memberId, List<Long> roleIds);

    /**
     * 当前员工在当前企业工作台下的权限快照。
     *
     * <p>该对象返回给前端，用于菜单、按钮、路由等显示控制。真正的后端接口权限判断
     * 仍由 {@code @RequirePermission} + {@code AccessContextHolder} + RBAC 服务完成，
     * 不能把前端持有的 permissions 当成可信授权依据。</p>
     */
    record MyPermissionsVO(
        /**
         * 当前企业工作台编号，来自 {@code X-Organization-No} 解析后的企业上下文。
         */
        String organizationNo,

        /**
         * 当前工作台模式。
         *
         * <p>{@code FULL} 表示企业认证已通过，可结合权限展示完整后台；
         * {@code CERTIFICATION_ONLY} 表示企业尚未认证通过，只展示资料/认证相关功能。</p>
         */
        OrganizationWorkspaceMode workspaceMode,

        /**
         * 当前企业是否认证通过。
         */
        boolean certified,

        /**
         * 当前员工在当前企业边界下拥有的精确权限点集合。
         *
         * <p>后端会把角色中的通配符授权展开成精确权限点，例如
         * {@code organization.member.*} 会展开成 {@code organization.member.read} 等具体 code，
         * 前端 {@code can()} 只判断这些精确权限点。</p>
         */
        List<String> permissions
    ) {
    }

    record RoleVO(
        Long id,
        String roleCode,
        String roleName,
        Boolean systemPreset,
        AccessRolePO.Status status,
        List<String> permissionPatterns,
        long assignmentCount
    ) {
    }

    record AssignedRoleVO(Long id, String roleCode, String roleName) {
    }

    /**
     * 角色权限页的员工授权主体视图。
     *
     * <p>它由 organization 的员工基础信息加上 access 的已分配角色组成，只服务
     * {@code /workspace/access/members} 授权列表，不回流到 organization 员工管理接口。</p>
     */
    record AssignmentMemberVO(
        Long memberId,
        String memberName,
        String phone,
        Long departmentId,
        OrganizationMemberPO.Status status,
        List<AssignedRoleVO> roles
    ) {
    }

    record MemberRolesVO(Long memberId, List<Long> roleIds, List<AssignedRoleVO> roles) {
    }
}
