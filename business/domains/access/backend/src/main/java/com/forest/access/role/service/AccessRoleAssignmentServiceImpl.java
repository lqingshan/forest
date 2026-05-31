package com.forest.access.role.service;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.core.AccessSubjectType;
import com.forest.access.role.entity.AccessRoleAssignmentPO;
import com.forest.access.role.entity.AccessRolePO;
import com.forest.access.role.repository.AccessRoleAssignmentRepository;
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
import static com.forest.starter.jpa.ForestAuditablePO.Deleted.DELETED;

/**
 * 默认 RBAC 主体角色授权服务实现。
 */
@Service
public class AccessRoleAssignmentServiceImpl implements AccessRoleAssignmentService {
    private final AccessRoleRepository roleRepository;
    private final AccessRoleAssignmentRepository roleAssignmentRepository;

    public AccessRoleAssignmentServiceImpl(
        AccessRoleRepository roleRepository,
        AccessRoleAssignmentRepository roleAssignmentRepository
    ) {
        this.roleRepository = roleRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignedRole> listAssignedRoles(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId
    ) {
        List<AccessRoleAssignmentPO> assignments = roleAssignmentRepository
            .findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryIdAndDeleted(
                subjectType,
                subjectId,
                boundaryType,
                boundaryId,
                ACTIVE.value()
            );
        return toAssignedRoles(assignments);
    }

    @Override
    @Transactional
    public List<AssignedRole> replaceAssignedRoles(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        List<Long> roleIds,
        Long operatorUserId
    ) {
        Set<Long> requestedRoleIds = normalizeRoleIds(roleIds, boundaryType, boundaryId);
        List<AccessRoleAssignmentPO> existingAssignments = roleAssignmentRepository
            .findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryId(subjectType, subjectId, boundaryType, boundaryId);

        for (AccessRoleAssignmentPO assignment : existingAssignments) {
            if (isActive(assignment) && !requestedRoleIds.contains(assignment.getRoleId())) {
                assignment.setDeleted(DELETED.value());
                assignment.setModifiedId(operatorUserId);
                roleAssignmentRepository.save(assignment);
            }
        }
        for (Long roleId : requestedRoleIds) {
            upsertAssignment(subjectType, subjectId, boundaryType, boundaryId, roleId, operatorUserId);
        }
        return listAssignedRoles(subjectType, subjectId, boundaryType, boundaryId);
    }

    private Set<Long> normalizeRoleIds(List<Long> roleIds, AccessBoundaryType boundaryType, Long boundaryId) {
        Set<Long> result = new LinkedHashSet<>();
        if (roleIds == null) {
            return result;
        }
        for (Long roleId : roleIds) {
            AccessRolePO role = roleRepository.findByIdAndBoundaryTypeAndBoundaryIdAndDeleted(
                    roleId,
                    boundaryType,
                    boundaryId,
                    ACTIVE.value()
                )
                .filter(item -> item.getStatus() == AccessRolePO.Status.ACTIVE)
                .orElseThrow(() -> BusinessException.of("角色不存在或已停用"));
            result.add(role.getId());
        }
        return result;
    }

    private void upsertAssignment(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        Long roleId,
        Long operatorUserId
    ) {
        AccessRoleAssignmentPO assignment = roleAssignmentRepository
            .findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryIdAndRoleId(
                subjectType,
                subjectId,
                boundaryType,
                boundaryId,
                roleId
            )
            .orElseGet(() -> {
                AccessRoleAssignmentPO created = new AccessRoleAssignmentPO();
                created.setSubjectType(subjectType);
                created.setSubjectId(subjectId);
                created.setBoundaryType(boundaryType);
                created.setBoundaryId(boundaryId);
                created.setRoleId(roleId);
                created.setCreatedId(operatorUserId);
                return created;
            });
        assignment.setDeleted(ACTIVE.value());
        assignment.setModifiedId(operatorUserId);
        roleAssignmentRepository.save(assignment);
    }

    private List<AssignedRole> toAssignedRoles(List<AccessRoleAssignmentPO> assignments) {
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
            .map(role -> new AssignedRole(role.getId(), role.getRoleCode(), role.getRoleName()))
            .toList();
    }

    private boolean isActive(AccessRoleAssignmentPO assignment) {
        return ACTIVE.matches(assignment.getDeleted());
    }
}
