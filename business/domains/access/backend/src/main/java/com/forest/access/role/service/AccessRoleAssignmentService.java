package com.forest.access.role.service;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.core.AccessSubjectType;

import java.util.List;

/**
 * RBAC 主体角色授权能力。
 *
 * <p>该服务只处理“某个主体在某个边界下拥有哪些角色”，不理解员工是否启用、
 * 是否最后一个企业所有者等 organization 规则。</p>
 */
public interface AccessRoleAssignmentService {
    List<AssignedRole> listAssignedRoles(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId
    );

    List<AssignedRole> replaceAssignedRoles(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        List<Long> roleIds,
        Long operatorUserId
    );

    record AssignedRole(Long id, String roleCode, String roleName) {
    }
}
