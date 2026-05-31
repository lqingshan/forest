package com.forest.organization.core.service;

import com.forest.organization.common.OrganizationNumberGenerator;
import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.core.repository.OrganizationRepository;
import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides organization core lifecycle capabilities.
 */
@Service
public class OrganizationCoreService {
    private static final Integer ACTIVE_DELETED = 0;

    private final OrganizationRepository organizationRepository;
    private final OrganizationNumberGenerator numberGenerator;

    public OrganizationCoreService(OrganizationRepository organizationRepository, OrganizationNumberGenerator numberGenerator) {
        this.organizationRepository = organizationRepository;
        this.numberGenerator = numberGenerator;
    }

    @Transactional
    public OrganizationPO create(String organizationName, Long ownerUserId) {
        OrganizationPO organization = new OrganizationPO();
        organization.setOrganizationNo(numberGenerator.nextOrganizationNo());
        organization.setOrganizationName(requireText(organizationName, "企业名称不能为空"));
        organization.setOwnerUserId(ownerUserId);
        organization.setCreatedId(ownerUserId);
        organization.setModifiedId(ownerUserId);
        organization.setStatus(OrganizationPO.Status.ACTIVE);
        organization.setCertificationStatus(OrganizationPO.CertificationStatus.NOT_SUBMITTED);
        return organizationRepository.save(organization);
    }

    @Transactional(readOnly = true)
    public OrganizationPO requireByNo(String organizationNo) {
        return organizationRepository.findByOrganizationNoAndDeleted(requireText(organizationNo, "企业编号不能为空"), ACTIVE_DELETED)
            .orElseThrow(() -> new BusinessException("企业不存在"));
    }

    @Transactional(readOnly = true)
    public OrganizationPO requireById(Long organizationId) {
        OrganizationPO organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new BusinessException("企业不存在"));
        if (!ACTIVE_DELETED.equals(organization.getDeleted())) {
            throw new BusinessException("企业不存在");
        }
        return organization;
    }

    @Transactional(readOnly = true)
    public Map<Long, OrganizationPO> getMap(Collection<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Map.of();
        }
        return organizationRepository.findAllById(organizationIds).stream()
            .filter(organization -> ACTIVE_DELETED.equals(organization.getDeleted()))
            .collect(Collectors.toMap(OrganizationPO::getId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<OrganizationPO> getAll(Collection<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }
        return organizationRepository.findAllById(organizationIds).stream()
            .filter(organization -> ACTIVE_DELETED.equals(organization.getDeleted()))
            .toList();
    }

    @Transactional
    public OrganizationPO updateName(Long organizationId, String organizationName, Long operatorUserId) {
        OrganizationPO organization = requireById(organizationId);
        organization.setOrganizationName(requireText(organizationName, "企业名称不能为空"));
        organization.setModifiedId(operatorUserId);
        return organizationRepository.save(organization);
    }

    @Transactional
    public OrganizationPO updateCertificationStatus(
        Long organizationId,
        OrganizationPO.CertificationStatus certificationStatus,
        Long certificationId,
        Long operatorUserId
    ) {
        OrganizationPO organization = requireById(organizationId);
        organization.setCertificationStatus(certificationStatus);
        organization.setCurrentCertificationId(certificationId);
        organization.setModifiedId(operatorUserId);
        return organizationRepository.save(organization);
    }

    public void requireActive(OrganizationPO organization) {
        if (organization.getStatus() != OrganizationPO.Status.ACTIVE) {
            throw new BusinessException("企业已停用");
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }
}
