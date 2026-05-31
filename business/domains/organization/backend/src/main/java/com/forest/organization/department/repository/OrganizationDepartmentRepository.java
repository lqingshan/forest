package com.forest.organization.department.repository;

import com.forest.organization.department.entity.OrganizationDepartmentPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Provides persistence access to organization departments.
 */
@Repository
public interface OrganizationDepartmentRepository extends JpaRepository<OrganizationDepartmentPO, Long> {
    List<OrganizationDepartmentPO> findByOrganizationIdAndDeletedOrderBySortOrderAscIdAsc(Long organizationId, Integer deleted);

    Optional<OrganizationDepartmentPO> findByOrganizationIdAndDefaultDepartmentAndDeleted(
        Long organizationId,
        Boolean defaultDepartment,
        Integer deleted
    );

    Optional<OrganizationDepartmentPO> findByIdAndOrganizationIdAndDeleted(Long id, Long organizationId, Integer deleted);

    boolean existsByParentIdAndDeleted(Long parentId, Integer deleted);
}
