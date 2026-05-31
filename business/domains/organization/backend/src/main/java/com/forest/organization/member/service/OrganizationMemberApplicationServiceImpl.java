package com.forest.organization.member.service;

import com.forest.access.role.service.AccessControlService;
import com.forest.access.role.service.AccessRoleCodes;
import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.core.service.OrganizationCoreService;
import com.forest.organization.department.entity.OrganizationDepartmentPO;
import com.forest.organization.department.service.OrganizationDepartmentService;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.user.identity.provisioning.ProvisionPhonePasswordIdentityCommand;
import com.forest.user.identity.provisioning.ProvisionedIdentity;
import com.forest.user.identity.provisioning.UserIdentityProvisioningService;
import com.forest.user.identity.query.UserIdentityProfile;
import com.forest.user.identity.query.UserIdentityQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 默认企业员工用例编排服务。
 */
@Service
public class OrganizationMemberApplicationServiceImpl implements OrganizationMemberApplicationService {
    private final OrganizationCoreService organizationCoreService;
    private final OrganizationDepartmentService departmentService;
    private final OrganizationMemberService memberService;
    private final UserIdentityProvisioningService identityProvisioningService;
    private final UserIdentityQueryService identityQueryService;
    private final AccessControlService accessControlService;

    public OrganizationMemberApplicationServiceImpl(
        OrganizationCoreService organizationCoreService,
        OrganizationDepartmentService departmentService,
        OrganizationMemberService memberService,
        UserIdentityProvisioningService identityProvisioningService,
        UserIdentityQueryService identityQueryService,
        AccessControlService accessControlService
    ) {
        this.organizationCoreService = organizationCoreService;
        this.departmentService = departmentService;
        this.memberService = memberService;
        this.identityProvisioningService = identityProvisioningService;
        this.identityQueryService = identityQueryService;
        this.accessControlService = accessControlService;
    }

    @Override
    @Transactional
    public OrganizationMemberPO addMember(AddMemberCommand command) {
        OrganizationPO organization = organizationCoreService.requireByNo(command.organizationNo());
        organizationCoreService.requireActive(organization);
        memberService.requireMember(organization.getId(), command.operatorUserId());

        ProvisionedIdentity identity = identityProvisioningService.provisionPhonePasswordIdentity(
            new ProvisionPhonePasswordIdentityCommand(command.phone(), command.name(), command.initialPassword())
        );
        OrganizationDepartmentPO department = departmentService.requireBelongsToOrganization(
            organization.getId(),
            command.departmentId()
        );
        OrganizationMemberPO member = memberService.createMember(new OrganizationMemberService.CreateMemberCommand(
            organization.getId(),
            identity.userId(),
            department.getId(),
            command.operatorUserId()
        ));
        accessControlService.assignOrganizationRole(
            organization.getId(),
            member.getId(),
            command.roleCode() == null || command.roleCode().isBlank() ? AccessRoleCodes.ORGANIZATION_MEMBER : command.roleCode().trim(),
            command.operatorUserId()
        );
        return member;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationMemberListItem> listMembers(String organizationNo, Long operatorUserId) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        memberService.requireMember(organization.getId(), operatorUserId);
        List<OrganizationMemberPO> members = memberService.listByOrganization(organization.getId());
        Map<Long, UserIdentityProfile> profiles = identityQueryService.getProfiles(members.stream()
            .map(OrganizationMemberPO::getUserId)
            .toList());
        return members.stream()
            .map(member -> toListItem(member, profiles.get(member.getUserId())))
            .toList();
    }

    @Override
    @Transactional
    public OrganizationMemberPO updateMember(UpdateMemberCommand command) {
        OrganizationPO organization = organizationCoreService.requireByNo(command.organizationNo());
        memberService.requireMember(organization.getId(), command.operatorUserId());
        OrganizationDepartmentPO department = departmentService.requireBelongsToOrganization(
            organization.getId(),
            command.departmentId()
        );
        OrganizationMemberPO member = memberService.updateMember(new OrganizationMemberService.UpdateMemberCommand(
            organization.getId(),
            command.memberId(),
            department.getId(),
            command.operatorUserId()
        ));
        if (command.roleCode() != null && !command.roleCode().isBlank()) {
            accessControlService.assignOrganizationRole(
                organization.getId(),
                member.getId(),
                command.roleCode().trim(),
                command.operatorUserId()
            );
        }
        return member;
    }

    @Override
    @Transactional
    public OrganizationMemberPO updateStatus(UpdateMemberStatusCommand command) {
        OrganizationPO organization = organizationCoreService.requireByNo(command.organizationNo());
        memberService.requireMember(organization.getId(), command.operatorUserId());
        return memberService.updateStatus(new OrganizationMemberService.UpdateMemberStatusCommand(
            organization.getId(),
            command.memberId(),
            command.status(),
            command.operatorUserId()
        ));
    }

    private OrganizationMemberListItem toListItem(OrganizationMemberPO member, UserIdentityProfile profile) {
        return new OrganizationMemberListItem(
            member.getId(),
            member.getMemberNo(),
            profile == null ? null : profile.name(),
            profile == null ? null : profile.phone(),
            member.getDepartmentId(),
            member.getStatus()
        );
    }
}
