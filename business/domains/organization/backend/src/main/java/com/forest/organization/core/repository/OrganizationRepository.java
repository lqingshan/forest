package com.forest.organization.core.repository;

import com.forest.organization.core.entity.OrganizationPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Provides persistence access to organizations.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationPO, Long> {
    Optional<OrganizationPO> findByOrganizationNoAndDeleted(String organizationNo, Integer deleted);
}
