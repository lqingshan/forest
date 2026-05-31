package com.forest.organization.certification.repository;

import com.forest.organization.certification.entity.OrganizationCertificationPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Provides persistence access to organization certification records.
 */
@Repository
public interface OrganizationCertificationRepository extends JpaRepository<OrganizationCertificationPO, Long> {
    Optional<OrganizationCertificationPO> findFirstByOrganizationIdAndDeletedOrderByCreatedTimeDescIdDesc(
        Long organizationId,
        Integer deleted
    );

    List<OrganizationCertificationPO> findByStatusAndDeletedOrderByCreatedTimeAscIdAsc(
        OrganizationCertificationPO.Status status,
        Integer deleted
    );
}
