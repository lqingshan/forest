package com.forest.access.role.service;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.permission.catalog.PermissionCatalog;
import com.forest.access.permission.registry.PermissionRegistry;
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
import java.util.Set;
import java.util.UUID;

import static com.forest.starter.jpa.ForestAuditablePO.Deleted.ACTIVE;
import static com.forest.starter.jpa.ForestAuditablePO.Deleted.DELETED;

/**
 * 默认 RBAC 角色管理服务实现。
 */
@Service
public class AccessRoleManagementServiceImpl implements AccessRoleManagementService {
    private static final String CUSTOM_ROLE_PREFIX = "custom_";

    private final AccessRoleRepository roleRepository;
    private final AccessRolePermissionRepository rolePermissionRepository;
    private final AccessRoleAssignmentRepository roleAssignmentRepository;
    private final PermissionRegistry permissionRegistry;

    public AccessRoleManagementServiceImpl(
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

    @Override
    @Transactional(readOnly = true)
    public List<RoleDetail> listRoles(AccessBoundaryType boundaryType, Long boundaryId) {
        return roleRepository.findByBoundaryTypeAndBoundaryIdAndDeletedOrderByIdAsc(boundaryType, boundaryId, ACTIVE.value())
            .stream()
            .map(this::toDetail)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetail requireRole(Long roleId, AccessBoundaryType boundaryType, Long boundaryId) {
        return toDetail(requireRoleEntity(roleId, boundaryType, boundaryId));
    }

    @Override
    @Transactional
    public RoleDetail createRole(
        AccessBoundaryType boundaryType,
        Long boundaryId,
        String roleName,
        List<String> permissionPatterns,
        Long operatorUserId,
        Set<PermissionCatalog> allowedCatalogs
    ) {
        AccessRolePO role = new AccessRolePO();
        role.setBoundaryType(boundaryType);
        role.setBoundaryId(boundaryId);
        role.setRoleCode(nextCustomRoleCode());
        role.setRoleName(requireText(roleName, "角色名称不能为空"));
        role.setSystemPreset(false);
        role.setStatus(AccessRolePO.Status.ACTIVE);
        role.setCreatedId(operatorUserId);
        role.setModifiedId(operatorUserId);
        AccessRolePO saved = roleRepository.save(role);
        replaceRolePermissionPatterns(saved, permissionPatterns, operatorUserId, allowedCatalogs);
        return toDetail(saved);
    }

    @Override
    @Transactional
    public RoleDetail updateRole(
        Long roleId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        String roleName,
        AccessRolePO.Status status,
        Long operatorUserId
    ) {
        AccessRolePO role = requireMutableRole(roleId, boundaryType, boundaryId);
        role.setRoleName(requireText(roleName, "角色名称不能为空"));
        if (status != null) {
            role.setStatus(status);
        }
        role.setModifiedId(operatorUserId);
        return toDetail(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleDetail replacePermissions(
        Long roleId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        List<String> permissionPatterns,
        Long operatorUserId,
        Set<PermissionCatalog> allowedCatalogs
    ) {
        AccessRolePO role = requireMutableRole(roleId, boundaryType, boundaryId);
        replaceRolePermissionPatterns(role, permissionPatterns, operatorUserId, allowedCatalogs);
        role.setModifiedId(operatorUserId);
        roleRepository.save(role);
        return toDetail(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId, AccessBoundaryType boundaryType, Long boundaryId, Long operatorUserId) {
        AccessRolePO role = requireMutableRole(roleId, boundaryType, boundaryId);
        long assignmentCount = roleAssignmentRepository.countByRoleIdAndDeleted(role.getId(), ACTIVE.value());
        if (assignmentCount > 0) {
            throw BusinessException.of("角色仍被员工使用，不能删除");
        }
        role.setDeleted(DELETED.value());
        role.setModifiedId(operatorUserId);
        roleRepository.save(role);
        for (AccessRolePermissionPO permission : rolePermissionRepository.findByRoleIdAndDeleted(role.getId(), ACTIVE.value())) {
            permission.setDeleted(DELETED.value());
            permission.setModifiedId(operatorUserId);
            rolePermissionRepository.save(permission);
        }
    }

    private AccessRolePO requireMutableRole(Long roleId, AccessBoundaryType boundaryType, Long boundaryId) {
        AccessRolePO role = requireRoleEntity(roleId, boundaryType, boundaryId);
        if (Boolean.TRUE.equals(role.getSystemPreset())) {
            throw BusinessException.of("系统预设角色不能修改");
        }
        return role;
    }

    private AccessRolePO requireRoleEntity(Long roleId, AccessBoundaryType boundaryType, Long boundaryId) {
        return roleRepository.findByIdAndBoundaryTypeAndBoundaryIdAndDeleted(roleId, boundaryType, boundaryId, ACTIVE.value())
            .orElseThrow(() -> BusinessException.of("角色不存在"));
    }

    private void replaceRolePermissionPatterns(
        AccessRolePO role,
        List<String> permissionPatterns,
        Long operatorUserId,
        Set<PermissionCatalog> allowedCatalogs
    ) {
        Set<String> requestedPatterns = normalizePatterns(permissionPatterns, allowedCatalogs);
        List<AccessRolePermissionPO> existingPermissions = rolePermissionRepository.findByRoleId(role.getId());
        for (AccessRolePermissionPO permission : existingPermissions) {
            if (isActive(permission) && !requestedPatterns.contains(permission.getPermissionPattern())) {
                permission.setDeleted(DELETED.value());
                permission.setModifiedId(operatorUserId);
                rolePermissionRepository.save(permission);
            }
        }
        for (String pattern : requestedPatterns) {
            upsertRolePermission(role.getId(), pattern, operatorUserId);
        }
    }

    /**
     * 规范化角色权限模式集合。
     *
     * <p>角色管理接口接收的是前端授权页提交的权限模式，可以是精确权限点，也可以是允许授权的
     * 前缀通配符。这里逐个调用 {@link PermissionRegistry#requireGrantablePattern(String, Set)}
     * 完成合法性校验、授权目录限制和空白裁剪，并使用 {@link LinkedHashSet} 去重且保留提交顺序。
     * 本方法不会把多个精确权限点反向压缩成 {@code xxx.*}，后端会按前端提交的合法模式原样保存。</p>
     */
    private Set<String> normalizePatterns(List<String> permissionPatterns, Set<PermissionCatalog> allowedCatalogs) {
        Set<String> result = new LinkedHashSet<>();
        if (permissionPatterns == null) {
            return result;
        }
        for (String pattern : permissionPatterns) {
            result.add(permissionRegistry.requireGrantablePattern(pattern, allowedCatalogs));
        }
        return result;
    }

    private void upsertRolePermission(Long roleId, String permissionPattern, Long operatorUserId) {
        AccessRolePermissionPO permission = rolePermissionRepository.findByRoleIdAndPermissionPattern(roleId, permissionPattern)
            .orElseGet(() -> {
                AccessRolePermissionPO created = new AccessRolePermissionPO();
                created.setRoleId(roleId);
                created.setPermissionPattern(permissionPattern);
                created.setCreatedId(operatorUserId);
                return created;
            });
        permission.setPatternType(permissionPattern.endsWith(".*")
            ? AccessRolePermissionPO.PatternType.WILDCARD
            : AccessRolePermissionPO.PatternType.EXACT);
        permission.setDeleted(ACTIVE.value());
        permission.setModifiedId(operatorUserId);
        rolePermissionRepository.save(permission);
    }

    private RoleDetail toDetail(AccessRolePO role) {
        List<String> permissionPatterns = rolePermissionRepository.findByRoleIdAndDeleted(role.getId(), ACTIVE.value())
            .stream()
            .map(AccessRolePermissionPO::getPermissionPattern)
            .sorted()
            .toList();
        long assignmentCount = roleAssignmentRepository.countByRoleIdAndDeleted(role.getId(), ACTIVE.value());
        return new RoleDetail(
            role.getId(),
            role.getRoleCode(),
            role.getRoleName(),
            role.getSystemPreset(),
            role.getStatus(),
            permissionPatterns,
            assignmentCount
        );
    }

    private String nextCustomRoleCode() {
        return CUSTOM_ROLE_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw BusinessException.of(message);
        }
        return value.trim();
    }

    private boolean isActive(AccessRolePermissionPO permission) {
        return ACTIVE.matches(permission.getDeleted());
    }
}
