package com.forest.organizationaccess.admin.controller;

import com.forest.access.annotation.RequirePermission;
import com.forest.access.permission.catalog.AccessPermissionCodes;
import com.forest.access.permission.registry.PermissionNode;
import com.forest.access.role.entity.AccessRolePO;
import com.forest.organization.workspace.gate.RequireOrganizationFeature;
import com.forest.organizationaccess.admin.service.OrganizationAccessAdminService;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 企业工作台角色权限聚合 API。
 *
 * <p>对外仍使用 {@code /api/admin/workspace/access} 路径，由 organization 工作台拦截器
 * 构建当前企业上下文，再在聚合服务中编排 access RBAC 能力。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.ADMIN + "/workspace/access")
@RequireOrganizationFeature
public class OrganizationAccessAdminController {
    private final OrganizationAccessAdminService organizationAccessAdminService;

    public OrganizationAccessAdminController(OrganizationAccessAdminService organizationAccessAdminService) {
        this.organizationAccessAdminService = organizationAccessAdminService;
    }

    @GetMapping("/my-permissions")
    @RequireOrganizationFeature(allowUncertified = true)
    public Result<OrganizationAccessAdminService.MyPermissionsVO> myPermissions() {
        return Result.success(organizationAccessAdminService.myPermissions());
    }

    @GetMapping("/permission-tree")
    @RequirePermission(AccessPermissionCodes.ACCESS_ROLE_READ)
    public Result<List<PermissionNode>> permissionTree() {
        return Result.success(organizationAccessAdminService.permissionTree());
    }

    @GetMapping("/roles")
    @RequirePermission(AccessPermissionCodes.ACCESS_ROLE_READ)
    public Result<List<OrganizationAccessAdminService.RoleVO>> listRoles() {
        return Result.success(organizationAccessAdminService.listRoles());
    }

    @GetMapping("/roles/{roleId}")
    @RequirePermission(AccessPermissionCodes.ACCESS_ROLE_READ)
    public Result<OrganizationAccessAdminService.RoleVO> getRole(@PathVariable("roleId") Long roleId) {
        return Result.success(organizationAccessAdminService.getRole(roleId));
    }

    @PostMapping("/roles")
    @RequirePermission(AccessPermissionCodes.ACCESS_ROLE_CREATE)
    public Result<OrganizationAccessAdminService.RoleVO> createRole(@RequestBody CreateRoleRequest request) {
        return Result.success(organizationAccessAdminService.createRole(request.roleName(), request.permissionPatterns()));
    }

    @PutMapping("/roles/{roleId}")
    @RequirePermission(AccessPermissionCodes.ACCESS_ROLE_UPDATE)
    public Result<OrganizationAccessAdminService.RoleVO> updateRole(
        @PathVariable("roleId") Long roleId,
        @RequestBody UpdateRoleRequest request
    ) {
        return Result.success(organizationAccessAdminService.updateRole(roleId, request.roleName(), request.status()));
    }

    @PutMapping("/roles/{roleId}/permissions")
    @RequirePermission(AccessPermissionCodes.ACCESS_ROLE_UPDATE)
    public Result<OrganizationAccessAdminService.RoleVO> replaceRolePermissions(
        @PathVariable("roleId") Long roleId,
        @RequestBody ReplaceRolePermissionsRequest request
    ) {
        return Result.success(organizationAccessAdminService.replaceRolePermissions(roleId, request.permissionPatterns()));
    }

    @DeleteMapping("/roles/{roleId}")
    @RequirePermission(AccessPermissionCodes.ACCESS_ROLE_DELETE)
    public Result<ActionVO> deleteRole(@PathVariable("roleId") Long roleId) {
        organizationAccessAdminService.deleteRole(roleId);
        return Result.success(new ActionVO(true));
    }

    @GetMapping("/members")
    @RequirePermission(AccessPermissionCodes.ACCESS_ASSIGNMENT_READ)
    public Result<List<OrganizationAccessAdminService.AssignmentMemberVO>> listMembers() {
        return Result.success(organizationAccessAdminService.listMembers());
    }

    @GetMapping("/members/{memberId}/roles")
    @RequirePermission(AccessPermissionCodes.ACCESS_ASSIGNMENT_READ)
    public Result<OrganizationAccessAdminService.MemberRolesVO> listMemberRoles(@PathVariable("memberId") Long memberId) {
        return Result.success(organizationAccessAdminService.listMemberRoles(memberId));
    }

    @PutMapping("/members/{memberId}/roles")
    @RequirePermission(AccessPermissionCodes.ACCESS_ASSIGNMENT_MANAGE)
    public Result<OrganizationAccessAdminService.MemberRolesVO> replaceMemberRoles(
        @PathVariable("memberId") Long memberId,
        @RequestBody ReplaceMemberRolesRequest request
    ) {
        return Result.success(organizationAccessAdminService.replaceMemberRoles(memberId, request.roleIds()));
    }

    public record CreateRoleRequest(String roleName, List<String> permissionPatterns) {
    }

    public record UpdateRoleRequest(String roleName, AccessRolePO.Status status) {
    }

    public record ReplaceRolePermissionsRequest(List<String> permissionPatterns) {
    }

    public record ReplaceMemberRolesRequest(List<Long> roleIds) {
    }

    public record ActionVO(boolean success) {
    }
}
