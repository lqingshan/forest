package com.forest.access.role.repository;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.core.AccessSubjectType;
import com.forest.access.role.entity.AccessRoleAssignmentPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRoleAssignmentRepository extends JpaRepository<AccessRoleAssignmentPO, Long> {
    Optional<AccessRoleAssignmentPO> findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryIdAndRoleIdAndDeleted(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        Long roleId,
        Integer deleted
    );

    Optional<AccessRoleAssignmentPO> findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryIdAndRoleId(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        Long roleId
    );

    List<AccessRoleAssignmentPO> findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryIdAndDeleted(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        Integer deleted
    );

    List<AccessRoleAssignmentPO> findBySubjectTypeAndSubjectIdAndBoundaryTypeAndBoundaryId(
        AccessSubjectType subjectType,
        Long subjectId,
        AccessBoundaryType boundaryType,
        Long boundaryId
    );

    long countByBoundaryTypeAndBoundaryIdAndRoleIdAndDeleted(
        AccessBoundaryType boundaryType,
        Long boundaryId,
        Long roleId,
        Integer deleted
    );

    long countByRoleIdAndDeleted(Long roleId, Integer deleted);
}
