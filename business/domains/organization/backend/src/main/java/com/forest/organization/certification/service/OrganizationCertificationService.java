package com.forest.organization.certification.service;

import com.forest.organization.certification.entity.OrganizationCertificationPO;

import java.util.List;

/**
 * 定义企业认证提交与平台审核能力。
 */
public interface OrganizationCertificationService {
    OrganizationCertificationPO submit(SubmitCertificationCommand command);

    OrganizationCertificationPO getLatest(String organizationNo, Long operatorUserId);

    List<OrganizationCertificationPO> listPending();

    OrganizationCertificationPO requireById(Long certificationId);

    OrganizationCertificationPO approve(Long certificationId, Long reviewerUserId, String remark);

    OrganizationCertificationPO reject(Long certificationId, Long reviewerUserId, String remark);

    record SubmitCertificationCommand(
        String organizationNo,
        Long operatorUserId,
        String companyName,
        String unifiedSocialCreditCode,
        String legalRepresentativeName,
        String businessLicenseFileNo,
        String contactName,
        String contactPhone
    ) {
    }
}
