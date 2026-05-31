package com.forest.organization.workspace.service;

import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.core.service.OrganizationCoreService;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.member.service.OrganizationMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationWorkspaceServiceImpl implements OrganizationWorkspaceService {
    private final OrganizationCoreService organizationCoreService;
    private final OrganizationMemberService memberService;

    public OrganizationWorkspaceServiceImpl(
        OrganizationCoreService organizationCoreService,
        OrganizationMemberService memberService
    ) {
        this.organizationCoreService = organizationCoreService;
        this.memberService = memberService;
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationWorkspaceState resolve(String organizationNo, Long userId) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        organizationCoreService.requireActive(organization);
        OrganizationMemberPO member = memberService.requireMember(organization.getId(), userId);
        return OrganizationWorkspaceState.of(organization, member);
    }
}
