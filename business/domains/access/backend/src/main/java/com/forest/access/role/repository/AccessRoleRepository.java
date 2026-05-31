package com.forest.access.role.repository;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.role.entity.AccessRolePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRoleRepository extends JpaRepository<AccessRolePO, Long> {
    Optional<AccessRolePO> findByBoundaryTypeAndBoundaryIdAndRoleCodeAndDeleted(
        AccessBoundaryType boundaryType,
        Long boundaryId,
        String roleCode,
        Integer deleted
    );

    Optional<AccessRolePO> findByIdAndBoundaryTypeAndBoundaryIdAndDeleted(
        Long id,
        AccessBoundaryType boundaryType,
        Long boundaryId,
        Integer deleted
    );

    List<AccessRolePO> findByBoundaryTypeAndBoundaryIdAndDeletedOrderByIdAsc(
        AccessBoundaryType boundaryType,
        Long boundaryId,
        Integer deleted
    );

    List<AccessRolePO> findByIdInAndDeleted(List<Long> ids, Integer deleted);
}
