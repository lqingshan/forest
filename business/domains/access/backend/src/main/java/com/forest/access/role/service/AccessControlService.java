package com.forest.access.role.service;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.core.AccessCheckContext;
import com.forest.access.core.AccessSubjectType;
import com.forest.access.permission.catalog.AccessPermissionCodes;
import com.forest.access.permission.check.PermissionChecker;
import com.forest.access.permission.registry.PermissionRegistry;
import com.forest.access.role.entity.AccessRoleAssignmentPO;
import com.forest.access.role.entity.AccessRolePO;
import com.forest.access.role.entity.AccessRolePermissionPO;
import com.forest.access.role.repository.AccessRoleAssignmentRepository;
import com.forest.access.role.repository.AccessRolePermissionRepository;
import com.forest.access.role.repository.AccessRoleRepository;
import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.forest.starter.jpa.ForestAuditablePO.Deleted.ACTIVE;

@Service
public class AccessControlService implements PermissionChecker {
    private final AccessRoleRepository roleRepository;
    private final AccessRolePermissionRepository rolePermissionRepository;
    private final AccessRoleAssignmentRepository roleAssignmentRepository;
    private final PermissionRegistry permissionRegistry;

    public AccessControlService(
        AccessRoleRepository roleRepository,
        AccessRolePermissionRepository rolePermissionRepository,
        AccessRoleAssignmentRepository roleAssignmentRepository,
        PermissionRegistry permissionRegistry
    ) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.permissionRegistry = permissionRegistry;
    }

    @Transactional
    public void initializeOrganizationAccess(Long organizationId, Long ownerMemberId, Long operatorUserId) {
        AccessRolePO owner = ensureOrganizationRole(
            organizationId,
            AccessRoleCodes.ORGANIZATION_OWNER,
            "企业所有者",
            List.of("organization.*", "access.*"),
            operatorUserId
        );
        ensureOrganizationRole(
            organizationId,
            AccessRoleCodes.ORGANIZATION_ADMIN,
            "企业管理员",
            List.of(
                AccessPermissionCodes.ORGANIZATION_READ,
                AccessPermissionCodes.ORGANIZATION_UPDATE,
                AccessPermissionCodes.ORGANIZATION_CERTIFICATION_SUBMIT,
                "organization.department.*",
                "organization.member.*"
            ),
            operatorUserId
        );
        ensureOrganizationRole(
            organizationId,
            AccessRoleCodes.ORGANIZATION_MEMBER,
            "普通员工",
            List.of(
                AccessPermissionCodes.ORGANIZATION_READ,
                AccessPermissionCodes.ORGANIZATION_DEPARTMENT_READ,
                AccessPermissionCodes.ORGANIZATION_MEMBER_READ
            ),
            operatorUserId
        );
        assignRole(AccessSubjectType.ORGANIZATION_MEMBER, ownerMemberId, AccessBoundaryType.ORGANIZATION, organizationId, owner.getId(), operatorUserId);
    }

    @Transactional
    public void assignOrganizationRole(Long organizationId, Long memberId, String roleCode, Long operatorUserId) {
        AccessRolePO role = requireRole(AccessBoundaryType.ORGANIZATION, organizationId, roleCode);
        assignRole(AccessSubjectType.ORGANIZATION_MEMBER, memberId, AccessBoundaryType.ORGANIZATION, organizationId, role.getId(), operatorUserId);
    }

    @Transactional(readOnly = true)
    public long countOrganizationRoleAssignments(Long organizationId, String roleCode) {
        AccessRolePO role = requireRole(AccessBoundaryType.ORGANIZATION, organizationId, roleCode);
        return roleAssignmentRepository.countByBoundaryTypeAndBoundaryIdAndRoleIdAndDeleted(
            AccessBoundaryType.ORGANIZATION,
            organizationId,
            role.getId(),
            ACTIVE.value()
        );
    }

    @Transactional(readOnly = true)
    public boolean hasOrganizationRole(Long organizationId, Long memberId, String roleCode) {
        AccessRolePO role = requireRole(AccessBoundaryType.ORGANIZATION, organizationId, roleCode);
        return roleAssignmentRepository
            .findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryIdAndRoleIdAndDeleted(
                AccessSubjectType.ORGANIZATION_MEMBER,
                memberId,
                AccessBoundaryType.ORGANIZATION,
                organizationId,
                role.getId(),
                ACTIVE.value()
            )
            .isPresent();
    }

    @Transactional(readOnly = true)
    public List<AccessRoleSummary> listAssignedRoles(AccessSubjectType subjectType, Long subjectId, AccessBoundaryType boundaryType, Long boundaryId) {
        List<AccessRoleAssignmentPO> assignments = roleAssignmentRepository
            .findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryIdAndDeleted(subjectType, subjectId, boundaryType, boundaryId, ACTIVE.value());
        if (assignments.isEmpty()) {
            return List.of();
        }
        Map<Long, AccessRolePO> roleMap = roleRepository.findByIdInAndDeleted(assignments.stream()
                .map(AccessRoleAssignmentPO::getRoleId)
                .distinct()
                .toList(), ACTIVE.value())
            .stream()
            .filter(role -> role.getStatus() == AccessRolePO.Status.ACTIVE)
            .collect(Collectors.toMap(AccessRolePO::getId, Function.identity()));
        return assignments.stream()
            .map(assignment -> roleMap.get(assignment.getRoleId()))
            .filter(role -> role != null)
            .map(role -> new AccessRoleSummary(role.getId(), role.getRoleCode(), role.getRoleName()))
            .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean hasPermission(AccessCheckContext context, String permissionCode) {
        permissionRegistry.require(permissionCode);
        return listPermissionCodes(context).contains(permissionCode);
    }

    @Transactional(readOnly = true)
    public Set<String> listPermissionCodes(AccessCheckContext context) {
        List<AccessRoleAssignmentPO> assignments = roleAssignmentRepository
            .findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryIdAndDeleted(
                context.subjectType(),
                context.subjectId(),
                context.boundaryType(),
                context.boundaryId(),
                ACTIVE.value()
            );
        if (assignments.isEmpty()) {
            return Set.of();
        }
        List<Long> roleIds = assignments.stream()
            .map(AccessRoleAssignmentPO::getRoleId)
            .distinct()
            .toList();
        Set<Long> activeRoleIds = roleRepository.findByIdInAndDeleted(roleIds, ACTIVE.value()).stream()
            .filter(role -> role.getStatus() == AccessRolePO.Status.ACTIVE)
            .map(AccessRolePO::getId)
            .collect(Collectors.toSet());
        if (activeRoleIds.isEmpty()) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        for (AccessRolePermissionPO permission : rolePermissionRepository.findByRoleIdInAndDeleted(List.copyOf(activeRoleIds), ACTIVE.value())) {
            result.addAll(permissionRegistry.expand(permission.getPermissionPattern()));
        }
        return result;
    }

    private AccessRolePO ensureOrganizationRole(Long organizationId, String roleCode, String roleName, List<String> permissionPatterns, Long operatorUserId) {
        AccessRolePO role = roleRepository
            .findByBoundaryTypeAndBoundaryIdAndRoleCodeAndDeleted(AccessBoundaryType.ORGANIZATION, organizationId, roleCode, ACTIVE.value())
            .orElseGet(() -> createRole(AccessBoundaryType.ORGANIZATION, organizationId, roleCode, roleName, operatorUserId));
        for (String permissionPattern : permissionPatterns) {
            ensureRolePermission(role.getId(), permissionPattern, operatorUserId);
        }
        return role;
    }

    private AccessRolePO createRole(
        AccessBoundaryType boundaryType,
        Long boundaryId,
        String roleCode,
        String roleName,
        Long operatorUserId
    ) {
        AccessRolePO role = new AccessRolePO();
        role.setBoundaryType(boundaryType);
        role.setBoundaryId(boundaryId);
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setSystemPreset(true);
        role.setStatus(AccessRolePO.Status.ACTIVE);
        role.setCreatedId(operatorUserId);
        role.setModifiedId(operatorUserId);
        return roleRepository.save(role);
    }

    /**
     * 确保角色拥有指定权限模式。
     *
     * <p>这里用于默认角色初始化和幂等补偿：先通过 {@link PermissionRegistry#expand(String)}
     * 校验权限模式是否合法，再按 {@code roleId + permissionPattern} 查找历史记录。由于
     * {@code access_role_permission} 对这两个字段有唯一约束，曾经被软删除的记录不能重新
     * insert，只能恢复为有效状态，避免重复初始化或重新授权时触发唯一键冲突。</p>
     */
    private void ensureRolePermission(Long roleId, String permissionPattern, Long operatorUserId) {
        permissionRegistry.expand(permissionPattern);
        rolePermissionRepository.findByRoleIdAndPermissionPattern(roleId, permissionPattern)
            .map(permission -> {
                permission.setDeleted(ACTIVE.value());
                permission.setModifiedId(operatorUserId);
                return rolePermissionRepository.save(permission);
            })
            .orElseGet(() -> {
                AccessRolePermissionPO permission = new AccessRolePermissionPO();
                permission.setRoleId(roleId);
                permission.setPermissionPattern(permissionPattern);
                permission.setPatternType(permissionPattern.endsWith(".*") || "*".equals(permissionPattern)
                    ? AccessRolePermissionPO.PatternType.WILDCARD
                    : AccessRolePermissionPO.PatternType.EXACT);
                permission.setCreatedId(operatorUserId);
                permission.setModifiedId(operatorUserId);
                return rolePermissionRepository.save(permission);
            });
    }

    private AccessRolePO requireRole(AccessBoundaryType boundaryType, Long boundaryId, String roleCode) {
        return roleRepository.findByBoundaryTypeAndBoundaryIdAndRoleCodeAndDeleted(boundaryType, boundaryId, roleCode, ACTIVE.value())
            .filter(role -> role.getStatus() == AccessRolePO.Status.ACTIVE)
            .orElseThrow(() -> new BusinessException("角色不存在或已停用"));
    }

    /**
     * 确保指定主体在指定边界下拥有目标角色。
     *
     * <p>这里用于默认角色初始化和幂等补偿：授权唯一性由
     * {@code subjectType + subjectId + boundaryType + boundaryId + roleId} 保证。曾经被
     * 软删除的授权记录不能重新 insert，只能恢复为有效状态，避免重复初始化或重新分配角色时
     * 触发唯一键冲突。</p>
     */
    private void assignRole(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        Long roleId,
        Long operatorUserId
    ) {
        roleAssignmentRepository
            .findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryIdAndRoleId(
                subjectType,
                subjectId,
                boundaryType,
                boundaryId,
                roleId
            )
            .map(assignment -> {
                assignment.setDeleted(ACTIVE.value());
                assignment.setModifiedId(operatorUserId);
                return roleAssignmentRepository.save(assignment);
            })
            .orElseGet(() -> {
                AccessRoleAssignmentPO assignment = new AccessRoleAssignmentPO();
                assignment.setSubjectType(subjectType);
                assignment.setSubjectId(subjectId);
                assignment.setBoundaryType(boundaryType);
                assignment.setBoundaryId(boundaryId);
                assignment.setRoleId(roleId);
                assignment.setCreatedId(operatorUserId);
                assignment.setModifiedId(operatorUserId);
                return roleAssignmentRepository.save(assignment);
            });
    }
}
