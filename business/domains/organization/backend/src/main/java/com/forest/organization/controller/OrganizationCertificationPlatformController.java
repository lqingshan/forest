package com.forest.organization.controller;

import com.forest.access.annotation.RequirePermission;
import com.forest.access.permission.catalog.AccessPermissionCodes;
import com.forest.organization.certification.entity.OrganizationCertificationPO;
import com.forest.organization.certification.service.OrganizationCertificationService;
import com.forest.starter.common.Result;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台端企业认证审核 API。
 *
 * <p>该 Controller 属于平台治理入口，路径位于 {@code /api/platform/**}，不属于企业工作台
 * {@code /api/admin/workspace/**}。请求会先经过 platform token 校验，再由平台访问上下文拦截器
 * 写入 {@code AccessCheckContext(ORGANIZATION_MEMBER, platformMemberId, PLATFORM, boundaryId)}，
 * 最后由 {@link RequirePermission} 执行具体平台权限点校验。</p>
 *
 * <p>这里操作的是商家企业提交的认证申请，但权限主体仍然是配置的平台企业员工身份；
 * 不读取 {@code X-Organization-No}，也不写入 {@code OrganizationWorkspaceContext}。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.PLATFORM + "/organization-certification")
public class OrganizationCertificationPlatformController {
    private final CurrentPrincipal currentAuth;
    private final OrganizationCertificationService certificationService;

    public OrganizationCertificationPlatformController(CurrentPrincipal currentAuth, OrganizationCertificationService certificationService) {
        this.currentAuth = currentAuth;
        this.certificationService = certificationService;
    }

    /**
     * 查询所有待审核企业认证申请。
     *
     * <p>需要平台治理边界下的认证申请查看权限。</p>
     */
    @GetMapping
    @RequirePermission(AccessPermissionCodes.PLATFORM_ORGANIZATION_CERTIFICATION_READ)
    public Result<List<CertificationVO>> listPending() {
        return Result.success(certificationService.listPending().stream()
            .map(CertificationVO::from)
            .toList());
    }

    /**
     * 查看指定企业认证申请详情。
     *
     * <p>用于平台审核员进入审核详情页前读取申请资料。</p>
     */
    @GetMapping("/{certificationId}")
    @RequirePermission(AccessPermissionCodes.PLATFORM_ORGANIZATION_CERTIFICATION_READ)
    public Result<CertificationVO> get(@PathVariable("certificationId") Long certificationId) {
        return Result.success(CertificationVO.from(certificationService.requireById(certificationId)));
    }

    /**
     * 审核通过企业认证申请。
     *
     * <p>审核人取当前 platform token 对应的 userId，具体是否有审核权限由 RBAC 判断。</p>
     */
    @PostMapping("/{certificationId}/approve")
    @RequirePermission(AccessPermissionCodes.PLATFORM_ORGANIZATION_CERTIFICATION_REVIEW)
    public Result<CertificationVO> approve(
        @PathVariable("certificationId") Long certificationId,
        @RequestBody ReviewRequest request
    ) {
        OrganizationCertificationPO certification = certificationService.approve(
            certificationId,
            currentAuth.requireUserId(),
            request.reviewRemark()
        );
        return Result.success(CertificationVO.from(certification));
    }

    /**
     * 驳回企业认证申请。
     *
     * <p>驳回后企业仍可在受限工作台中查看状态并重新提交认证资料。</p>
     */
    @PostMapping("/{certificationId}/reject")
    @RequirePermission(AccessPermissionCodes.PLATFORM_ORGANIZATION_CERTIFICATION_REVIEW)
    public Result<CertificationVO> reject(
        @PathVariable("certificationId") Long certificationId,
        @RequestBody ReviewRequest request
    ) {
        OrganizationCertificationPO certification = certificationService.reject(
            certificationId,
            currentAuth.requireUserId(),
            request.reviewRemark()
        );
        return Result.success(CertificationVO.from(certification));
    }

    public record CertificationVO(
        Long id,
        String certificationNo,
        Long organizationId,
        String companyName,
        String unifiedSocialCreditCode,
        String legalRepresentativeName,
        String businessLicenseFileNo,
        String contactName,
        String contactPhone,
        OrganizationCertificationPO.Status status,
        Long submittedByUserId,
        Long reviewedByUserId,
        java.time.LocalDateTime reviewedTime,
        String reviewRemark,
        java.time.LocalDateTime createdTime
    ) {
        public static CertificationVO from(OrganizationCertificationPO certification) {
            return new CertificationVO(
                certification.getId(),
                certification.getCertificationNo(),
                certification.getOrganizationId(),
                certification.getCompanyName(),
                certification.getUnifiedSocialCreditCode(),
                certification.getLegalRepresentativeName(),
                certification.getBusinessLicenseFileNo(),
                certification.getContactName(),
                certification.getContactPhone(),
                certification.getStatus(),
                certification.getSubmittedByUserId(),
                certification.getReviewedByUserId(),
                certification.getReviewedTime(),
                certification.getReviewRemark(),
                certification.getCreatedTime()
            );
        }
    }

    public record ReviewRequest(String reviewRemark) {
    }
}
