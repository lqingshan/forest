package com.forest.access.role.service;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.permission.catalog.PermissionCatalog;
import com.forest.access.role.entity.AccessRolePO;

import java.util.List;
import java.util.Set;

/**
 * RBAC 角色管理能力。
 *
 * <p>该服务只理解“某个权限边界下的角色”，不读取 organization workspace、
 * 不解析 URL，也不负责判断当前操作者是谁。</p>
 */
public interface AccessRoleManagementService {
    List<RoleDetail> listRoles(AccessBoundaryType boundaryType, Long boundaryId);

    RoleDetail requireRole(Long roleId, AccessBoundaryType boundaryType, Long boundaryId);

    RoleDetail createRole(
        AccessBoundaryType boundaryType,
        Long boundaryId,
        String roleName,
        List<String> permissionPatterns,
        Long operatorUserId,
        Set<PermissionCatalog> allowedCatalogs
    );

    RoleDetail updateRole(
        Long roleId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        String roleName,
        AccessRolePO.Status status,
        Long operatorUserId
    );

    RoleDetail replacePermissions(
        Long roleId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        List<String> permissionPatterns,
        Long operatorUserId,
        Set<PermissionCatalog> allowedCatalogs
    );

    void deleteRole(Long roleId, AccessBoundaryType boundaryType, Long boundaryId, Long operatorUserId);

    record RoleDetail(
        Long id,
        String roleCode,
        String roleName,
        Boolean systemPreset,
        AccessRolePO.Status status,
        List<String> permissionPatterns,
        long assignmentCount
    ) {
    }
}
