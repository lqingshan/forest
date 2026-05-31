package com.forest.organization.member.repository;

import com.forest.organization.member.entity.OrganizationMemberPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Provides persistence access to organization members.
 */
@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMemberPO, Long> {
    Optional<OrganizationMemberPO> findByOrganizationIdAndUserIdAndDeleted(Long organizationId, Long userId, Integer deleted);

    Optional<OrganizationMemberPO> findByIdAndOrganizationIdAndDeleted(Long id, Long organizationId, Integer deleted);

    List<OrganizationMemberPO> findByOrganizationIdAndDeletedOrderByIdAsc(Long organizationId, Integer deleted);

    List<OrganizationMemberPO> findByUserIdAndDeletedOrderByIdAsc(Long userId, Integer deleted);

    boolean existsByDepartmentIdAndDeleted(Long departmentId, Integer deleted);
}
