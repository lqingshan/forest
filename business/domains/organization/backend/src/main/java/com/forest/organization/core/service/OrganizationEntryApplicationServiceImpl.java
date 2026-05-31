package com.forest.organization.core.service;

import com.forest.business.common.event.organization.OrganizationCreatedEvent;
import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.department.entity.OrganizationDepartmentPO;
import com.forest.organization.department.service.OrganizationDepartmentService;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.member.service.OrganizationMemberService;
import com.forest.organization.workspace.service.OrganizationWorkspaceService;
import com.forest.organization.workspace.service.OrganizationWorkspaceState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 默认企业入口用例服务。
 */
@Service
public class OrganizationEntryApplicationServiceImpl implements OrganizationEntryApplicationService {
    private final OrganizationCoreService organizationCoreService;
    private final OrganizationDepartmentService departmentService;
    private final OrganizationMemberService memberService;
    private final OrganizationWorkspaceService organizationWorkspaceService;
    private final ApplicationEventPublisher eventPublisher;

    public OrganizationEntryApplicationServiceImpl(
        OrganizationCoreService organizationCoreService,
        OrganizationDepartmentService departmentService,
        OrganizationMemberService memberService,
        OrganizationWorkspaceService organizationWorkspaceService,
        ApplicationEventPublisher eventPublisher
    ) {
        this.organizationCoreService = organizationCoreService;
        this.departmentService = departmentService;
        this.memberService = memberService;
        this.organizationWorkspaceService = organizationWorkspaceService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrganizationPO createOrganization(String organizationName, Long ownerUserId) {
        OrganizationPO organization = organizationCoreService.create(organizationName, ownerUserId);
        OrganizationDepartmentPO department = departmentService.createDefaultRoot(organization.getId(), ownerUserId);
        OrganizationMemberPO owner = memberService.createOwner(organization.getId(), ownerUserId, department.getId());
        eventPublisher.publishEvent(OrganizationCreatedEvent.of(organization.getId(), owner.getId(), ownerUserId));
        organization.setCurrentCertificationId(null);
        organization.setModifiedId(owner.getUserId());
        return organization;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationPO> listMyOrganizations(Long userId) {
        List<Long> organizationIds = memberService.listByUser(userId).stream()
            .map(OrganizationMemberPO::getOrganizationId)
            .toList();
        Map<Long, OrganizationPO> organizations = organizationCoreService.getMap(organizationIds);
        return organizationIds.stream()
            .map(organizations::get)
            .filter(organization -> organization != null && organization.getStatus() == OrganizationPO.Status.ACTIVE)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationWorkspaceState enterWorkspace(String organizationNo, Long userId) {
        return organizationWorkspaceService.resolve(organizationNo, userId);
    }
}
