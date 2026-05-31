package com.forest.organization.controller;

import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.core.service.OrganizationEntryApplicationService;
import com.forest.organization.workspace.context.OrganizationWorkspaceMode;
import com.forest.organization.workspace.service.OrganizationWorkspaceState;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业入口 API。
 *
 * <p>这里的接口还没有进入某个企业工作台，因此不依赖 {@code X-Organization-No}。
 * 企业工作台内的资料、认证、部门和员工接口统一放在 {@code /api/admin/workspace} 下。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.ADMIN + "/organization")
public class OrganizationAdminController {
    private final CurrentPrincipal currentAuth;
    private final OrganizationEntryApplicationService organizationEntryApplicationService;

    public OrganizationAdminController(
        CurrentPrincipal currentAuth,
        OrganizationEntryApplicationService organizationEntryApplicationService
    ) {
        this.currentAuth = currentAuth;
        this.organizationEntryApplicationService = organizationEntryApplicationService;
    }

    @PostMapping
    public Result<OrganizationVO> createOrganization(@RequestBody CreateOrganizationRequest request) {
        OrganizationPO organization = organizationEntryApplicationService.createOrganization(
            request.organizationName(),
            currentAuth.requireUserId()
        );
        return Result.success(OrganizationVO.from(organization));
    }

    @GetMapping("/my")
    public Result<List<OrganizationVO>> listMyOrganizations() {
        return Result.success(organizationEntryApplicationService.listMyOrganizations(currentAuth.requireUserId()).stream()
            .map(OrganizationVO::from)
            .toList());
    }

    @PostMapping("/{organizationNo}/enter")
    public Result<WorkspaceEntryVO> enterWorkspace(@PathVariable("organizationNo") String organizationNo) {
        OrganizationWorkspaceState workspace = organizationEntryApplicationService.enterWorkspace(organizationNo, currentAuth.requireUserId());
        return Result.success(WorkspaceEntryVO.from(workspace));
    }

    public record CreateOrganizationRequest(String organizationName) {
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

    public record WorkspaceEntryVO(
        Long organizationId,
        String organizationNo,
        Long memberId,
        OrganizationWorkspaceMode workspaceMode,
        boolean certified
    ) {
        public static WorkspaceEntryVO from(OrganizationWorkspaceState workspace) {
            return new WorkspaceEntryVO(
                workspace.organization().getId(),
                workspace.organization().getOrganizationNo(),
                workspace.member().getId(),
                workspace.workspaceMode(),
                workspace.certified()
            );
        }
    }
}
