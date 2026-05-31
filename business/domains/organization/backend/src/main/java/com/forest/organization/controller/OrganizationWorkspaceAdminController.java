package com.forest.organization.controller;

import com.forest.access.annotation.RequirePermission;
import com.forest.access.permission.catalog.AccessPermissionCodes;
import com.forest.organization.certification.entity.OrganizationCertificationPO;
import com.forest.organization.certification.service.OrganizationCertificationService;
import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.core.service.OrganizationWorkspaceApplicationService;
import com.forest.organization.department.entity.OrganizationDepartmentPO;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.member.service.OrganizationMemberApplicationService;
import com.forest.organization.workspace.context.CurrentOrganizationWorkspace;
import com.forest.organization.workspace.context.OrganizationWorkspaceContext;
import com.forest.organization.workspace.gate.RequireOrganizationFeature;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业工作台 API。
 *
 * <p>这些接口都在用户选择企业之后调用，统一通过请求头 {@code X-Organization-No}
 * 声明当前企业。企业工作台拦截器负责构建当前企业上下文，企业认证 Gate 由
 * {@link com.forest.organization.workspace.gate.OrganizationWorkspaceAspect} 根据注解执行。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.ADMIN + "/workspace")
@RequireOrganizationFeature
public class OrganizationWorkspaceAdminController {
    private final CurrentOrganizationWorkspace currentWorkspace;
    private final OrganizationWorkspaceApplicationService organizationWorkspaceApplicationService;
    private final OrganizationCertificationService certificationService;
    private final OrganizationMemberApplicationService memberApplicationService;

    public OrganizationWorkspaceAdminController(
        CurrentOrganizationWorkspace currentWorkspace,
        OrganizationWorkspaceApplicationService organizationWorkspaceApplicationService,
        OrganizationCertificationService certificationService,
        OrganizationMemberApplicationService memberApplicationService
    ) {
        this.currentWorkspace = currentWorkspace;
        this.organizationWorkspaceApplicationService = organizationWorkspaceApplicationService;
        this.certificationService = certificationService;
        this.memberApplicationService = memberApplicationService;
    }

    @GetMapping("/organization")
    @RequireOrganizationFeature(allowUncertified = true)
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_READ)
    public Result<OrganizationVO> getOrganization() {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        return Result.success(OrganizationVO.from(organizationWorkspaceApplicationService.getOrganization(
            workspace.organizationNo(),
            workspace.userId()
        )));
    }

    @PutMapping("/organization")
    @RequireOrganizationFeature(allowUncertified = true)
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_UPDATE)
    public Result<OrganizationVO> updateOrganization(@RequestBody UpdateOrganizationRequest request) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        return Result.success(OrganizationVO.from(organizationWorkspaceApplicationService.updateOrganization(
            workspace.organizationNo(),
            request.organizationName(),
            workspace.userId()
        )));
    }

    @PostMapping("/certification")
    @RequireOrganizationFeature(allowUncertified = true)
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_CERTIFICATION_SUBMIT)
    public Result<CertificationVO> submitCertification(@RequestBody SubmitCertificationRequest request) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        OrganizationCertificationPO certification = certificationService.submit(new OrganizationCertificationService.SubmitCertificationCommand(
            workspace.organizationNo(),
            workspace.userId(),
            request.companyName(),
            request.unifiedSocialCreditCode(),
            request.legalRepresentativeName(),
            request.businessLicenseFileNo(),
            request.contactName(),
            request.contactPhone()
        ));
        return Result.success(CertificationVO.from(certification));
    }

    @GetMapping("/certification/latest")
    @RequireOrganizationFeature(allowUncertified = true)
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_READ)
    public Result<CertificationVO> latestCertification() {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        OrganizationCertificationPO certification = certificationService.getLatest(workspace.organizationNo(), workspace.userId());
        return Result.success(certification == null ? null : CertificationVO.from(certification));
    }

    @GetMapping("/department")
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_DEPARTMENT_READ)
    public Result<List<DepartmentVO>> listDepartments() {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        return Result.success(organizationWorkspaceApplicationService.listDepartments(workspace.organizationNo(), workspace.userId()).stream()
            .map(DepartmentVO::from)
            .toList());
    }

    @PostMapping("/department")
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_DEPARTMENT_CREATE)
    public Result<DepartmentVO> createDepartment(@RequestBody SaveDepartmentRequest request) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        return Result.success(DepartmentVO.from(organizationWorkspaceApplicationService.createDepartment(
            workspace.organizationNo(),
            request.parentId(),
            request.departmentName(),
            request.sortOrder(),
            workspace.userId()
        )));
    }

    @PutMapping("/department/{departmentId}")
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_DEPARTMENT_UPDATE)
    public Result<DepartmentVO> updateDepartment(
        @PathVariable("departmentId") Long departmentId,
        @RequestBody SaveDepartmentRequest request
    ) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        return Result.success(DepartmentVO.from(organizationWorkspaceApplicationService.updateDepartment(
            workspace.organizationNo(),
            departmentId,
            request.departmentName(),
            request.sortOrder(),
            workspace.userId()
        )));
    }

    @DeleteMapping("/department/{departmentId}")
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_DEPARTMENT_DELETE)
    public Result<ActionVO> deleteDepartment(@PathVariable("departmentId") Long departmentId) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        organizationWorkspaceApplicationService.deleteDepartment(workspace.organizationNo(), departmentId, workspace.userId());
        return Result.success(new ActionVO(true));
    }

    @GetMapping("/member")
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_MEMBER_READ)
    public Result<List<OrganizationMemberApplicationService.OrganizationMemberListItem>> listMembers() {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        return Result.success(memberApplicationService.listMembers(workspace.organizationNo(), workspace.userId()));
    }

    @PostMapping("/member")
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_MEMBER_CREATE)
    public Result<MemberVO> addMember(@RequestBody AddMemberRequest request) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        OrganizationMemberPO member = memberApplicationService.addMember(new OrganizationMemberApplicationService.AddMemberCommand(
            workspace.organizationNo(),
            workspace.userId(),
            request.phone(),
            request.name(),
            request.initialPassword(),
            request.departmentId(),
            request.roleCode()
        ));
        return Result.success(MemberVO.from(member));
    }

    @PutMapping("/member/{memberId}")
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_MEMBER_UPDATE)
    public Result<MemberVO> updateMember(
        @PathVariable("memberId") Long memberId,
        @RequestBody UpdateMemberRequest request
    ) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        OrganizationMemberPO member = memberApplicationService.updateMember(new OrganizationMemberApplicationService.UpdateMemberCommand(
            workspace.organizationNo(),
            workspace.userId(),
            memberId,
            request.departmentId(),
            request.roleCode()
        ));
        return Result.success(MemberVO.from(member));
    }

    @PostMapping("/member/{memberId}/disable")
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_MEMBER_DISABLE)
    public Result<MemberVO> disableMember(@PathVariable("memberId") Long memberId) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        return Result.success(MemberVO.from(memberApplicationService.updateStatus(new OrganizationMemberApplicationService.UpdateMemberStatusCommand(
            workspace.organizationNo(),
            memberId,
            OrganizationMemberPO.Status.DISABLED,
            workspace.userId()
        ))));
    }

    @PostMapping("/member/{memberId}/activate")
    @RequirePermission(AccessPermissionCodes.ORGANIZATION_MEMBER_ACTIVATE)
    public Result<MemberVO> activateMember(@PathVariable("memberId") Long memberId) {
        OrganizationWorkspaceContext workspace = currentWorkspace.require();
        return Result.success(MemberVO.from(memberApplicationService.updateStatus(new OrganizationMemberApplicationService.UpdateMemberStatusCommand(
            workspace.organizationNo(),
            memberId,
            OrganizationMemberPO.Status.ACTIVE,
            workspace.userId()
        ))));
    }

    public record UpdateOrganizationRequest(String organizationName) {
    }

    public record SubmitCertificationRequest(
        String companyName,
        String unifiedSocialCreditCode,
        String legalRepresentativeName,
        String businessLicenseFileNo,
        String contactName,
        String contactPhone
    ) {
    }

    public record SaveDepartmentRequest(Long parentId, String departmentName, Integer sortOrder) {
    }

    public record AddMemberRequest(
        String phone,
        String name,
        String initialPassword,
        Long departmentId,
        String roleCode
    ) {
    }

    public record UpdateMemberRequest(Long departmentId, String roleCode) {
    }

    public record OrganizationVO(
        Long id,
        String organizationNo,
        String organizationName,
        OrganizationPO.Status status,
        OrganizationPO.CertificationStatus certificationStatus,
        Long currentCertificationId,
        Long ownerUserId,
        LocalDateTime createdTime
    ) {
        public static OrganizationVO from(OrganizationPO organization) {
            return new OrganizationVO(
                organization.getId(),
                organization.getOrganizationNo(),
                organization.getOrganizationName(),
                organization.getStatus(),
                organization.getCertificationStatus(),
                organization.getCurrentCertificationId(),
                organization.getOwnerUserId(),
                organization.getCreatedTime()
            );
        }
    }

    public record DepartmentVO(
        Long id,
        String departmentNo,
        Long organizationId,
        Long parentId,
        String departmentName,
        Boolean defaultDepartment,
        Integer sortOrder,
        OrganizationDepartmentPO.Status status
    ) {
        public static DepartmentVO from(OrganizationDepartmentPO department) {
            return new DepartmentVO(
                department.getId(),
                department.getDepartmentNo(),
                department.getOrganizationId(),
                department.getParentId(),
                department.getDepartmentName(),
                department.getDefaultDepartment(),
                department.getSortOrder(),
                department.getStatus()
            );
        }
    }

    public record MemberVO(
        Long memberId,
        String memberNo,
        Long departmentId,
        OrganizationMemberPO.Status status
    ) {
        public static MemberVO from(OrganizationMemberPO member) {
            return new MemberVO(
                member.getId(),
                member.getMemberNo(),
                member.getDepartmentId(),
                member.getStatus()
            );
        }
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
        LocalDateTime reviewedTime,
        String reviewRemark,
        LocalDateTime createdTime
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

    public record ActionVO(boolean success) {
    }
}
