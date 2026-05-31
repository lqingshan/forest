package com.forest.organization.certification.service;

import com.forest.file.entity.FileCategory;
import com.forest.file.entity.FileStatus;
import com.forest.file.service.FileService;
import com.forest.organization.certification.entity.OrganizationCertificationPO;
import com.forest.organization.certification.repository.OrganizationCertificationRepository;
import com.forest.organization.common.OrganizationNumberGenerator;
import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.core.service.OrganizationCoreService;
import com.forest.organization.member.service.OrganizationMemberService;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.time.ForestTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 默认企业认证服务实现。
 */
@Service
public class OrganizationCertificationServiceImpl implements OrganizationCertificationService {
    private static final Integer ACTIVE_DELETED = 0;

    private final OrganizationCertificationRepository certificationRepository;
    private final OrganizationNumberGenerator numberGenerator;
    private final OrganizationCoreService organizationCoreService;
    private final OrganizationMemberService memberService;
    private final FileService fileService;

    public OrganizationCertificationServiceImpl(
        OrganizationCertificationRepository certificationRepository,
        OrganizationNumberGenerator numberGenerator,
        OrganizationCoreService organizationCoreService,
        OrganizationMemberService memberService,
        FileService fileService
    ) {
        this.certificationRepository = certificationRepository;
        this.numberGenerator = numberGenerator;
        this.organizationCoreService = organizationCoreService;
        this.memberService = memberService;
        this.fileService = fileService;
    }

    @Override
    @Transactional
    public OrganizationCertificationPO submit(SubmitCertificationCommand command) {
        OrganizationPO organization = organizationCoreService.requireByNo(command.organizationNo());
        organizationCoreService.requireActive(organization);
        memberService.requireMember(organization.getId(), command.operatorUserId());
        requireBusinessLicense(command.businessLicenseFileNo());

        OrganizationCertificationPO certification = new OrganizationCertificationPO();
        certification.setCertificationNo(numberGenerator.nextCertificationNo());
        certification.setOrganizationId(organization.getId());
        certification.setCompanyName(requireText(command.companyName(), "企业名称不能为空"));
        certification.setUnifiedSocialCreditCode(requireText(command.unifiedSocialCreditCode(), "统一社会信用代码不能为空"));
        certification.setLegalRepresentativeName(requireText(command.legalRepresentativeName(), "法人姓名不能为空"));
        certification.setBusinessLicenseFileNo(requireText(command.businessLicenseFileNo(), "营业执照不能为空"));
        certification.setContactName(requireText(command.contactName(), "联系人姓名不能为空"));
        certification.setContactPhone(requireText(command.contactPhone(), "联系人手机号不能为空"));
        certification.setStatus(OrganizationCertificationPO.Status.PENDING);
        certification.setSubmittedByUserId(command.operatorUserId());
        certification.setCreatedId(command.operatorUserId());
        certification.setModifiedId(command.operatorUserId());
        OrganizationCertificationPO saved = certificationRepository.save(certification);
        organizationCoreService.updateCertificationStatus(
            organization.getId(),
            OrganizationPO.CertificationStatus.PENDING,
            saved.getId(),
            command.operatorUserId()
        );
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationCertificationPO getLatest(String organizationNo, Long operatorUserId) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        memberService.requireMember(organization.getId(), operatorUserId);
        return certificationRepository.findFirstByOrganizationIdAndDeletedOrderByCreatedTimeDescIdDesc(
            organization.getId(),
            ACTIVE_DELETED
        ).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationCertificationPO> listPending() {
        return certificationRepository.findByStatusAndDeletedOrderByCreatedTimeAscIdAsc(
            OrganizationCertificationPO.Status.PENDING,
            ACTIVE_DELETED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationCertificationPO requireById(Long certificationId) {
        OrganizationCertificationPO certification = certificationRepository.findById(certificationId)
            .orElseThrow(() -> BusinessException.of("认证记录不存在"));
        if (!ACTIVE_DELETED.equals(certification.getDeleted())) {
            throw BusinessException.of("认证记录不存在");
        }
        return certification;
    }

    @Override
    @Transactional
    public OrganizationCertificationPO approve(Long certificationId, Long reviewerUserId, String remark) {
        OrganizationCertificationPO certification = requirePending(certificationId);
        certification.setStatus(OrganizationCertificationPO.Status.APPROVED);
        certification.setReviewedByUserId(reviewerUserId);
        certification.setReviewedTime(ForestTime.now());
        certification.setReviewRemark(trimToNull(remark));
        certification.setModifiedId(reviewerUserId);
        OrganizationCertificationPO saved = certificationRepository.save(certification);
        organizationCoreService.updateCertificationStatus(
            saved.getOrganizationId(),
            OrganizationPO.CertificationStatus.APPROVED,
            saved.getId(),
            reviewerUserId
        );
        return saved;
    }

    @Override
    @Transactional
    public OrganizationCertificationPO reject(Long certificationId, Long reviewerUserId, String remark) {
        OrganizationCertificationPO certification = requirePending(certificationId);
        certification.setStatus(OrganizationCertificationPO.Status.REJECTED);
        certification.setReviewedByUserId(reviewerUserId);
        certification.setReviewedTime(ForestTime.now());
        certification.setReviewRemark(requireText(remark, "驳回原因不能为空"));
        certification.setModifiedId(reviewerUserId);
        OrganizationCertificationPO saved = certificationRepository.save(certification);
        organizationCoreService.updateCertificationStatus(
            saved.getOrganizationId(),
            OrganizationPO.CertificationStatus.REJECTED,
            saved.getId(),
            reviewerUserId
        );
        return saved;
    }

    private OrganizationCertificationPO requirePending(Long certificationId) {
        OrganizationCertificationPO certification = requireById(certificationId);
        if (certification.getStatus() != OrganizationCertificationPO.Status.PENDING) {
            throw BusinessException.of("认证记录不是待审核状态");
        }
        return certification;
    }

    private void requireBusinessLicense(String fileNo) {
        FileService.FileInfo file = fileService.getFile(requireText(fileNo, "营业执照不能为空"));
        if (file.status() != FileStatus.AVAILABLE) {
            throw BusinessException.of("营业执照文件不可用");
        }
        if (file.fileCategory() != FileCategory.IMAGE && file.fileCategory() != FileCategory.DOCUMENT) {
            throw BusinessException.of("营业执照文件类型不正确");
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw BusinessException.of(message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
