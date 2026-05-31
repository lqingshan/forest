package com.forest.organization.core.service;

import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.department.entity.OrganizationDepartmentPO;
import com.forest.organization.department.service.OrganizationDepartmentService;
import com.forest.organization.member.service.OrganizationMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 默认企业工作台用例服务。
 */
@Service
public class OrganizationWorkspaceApplicationServiceImpl implements OrganizationWorkspaceApplicationService {
    private final OrganizationCoreService organizationCoreService;
    private final OrganizationDepartmentService departmentService;
    private final OrganizationMemberService memberService;

    public OrganizationWorkspaceApplicationServiceImpl(
        OrganizationCoreService organizationCoreService,
        OrganizationDepartmentService departmentService,
        OrganizationMemberService memberService
    ) {
        this.organizationCoreService = organizationCoreService;
        this.departmentService = departmentService;
        this.memberService = memberService;
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationPO getOrganization(String organizationNo, Long operatorUserId) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        memberService.requireMember(organization.getId(), operatorUserId);
        return organization;
    }

    @Override
    @Transactional
    public OrganizationPO updateOrganization(String organizationNo, String organizationName, Long operatorUserId) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        memberService.requireMember(organization.getId(), operatorUserId);
        return organizationCoreService.updateName(organization.getId(), organizationName, operatorUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationDepartmentPO> listDepartments(String organizationNo, Long operatorUserId) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        memberService.requireMember(organization.getId(), operatorUserId);
        return departmentService.list(organization.getId());
    }

    @Override
    @Transactional
    public OrganizationDepartmentPO createDepartment(
        String organizationNo,
        Long parentId,
        String departmentName,
        Integer sortOrder,
        Long operatorUserId
    ) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        memberService.requireMember(organization.getId(), operatorUserId);
        return departmentService.create(organization.getId(), parentId, departmentName, sortOrder, operatorUserId);
    }

    @Override
    @Transactional
    public OrganizationDepartmentPO updateDepartment(
        String organizationNo,
        Long departmentId,
        String departmentName,
        Integer sortOrder,
        Long operatorUserId
    ) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        memberService.requireMember(organization.getId(), operatorUserId);
        return departmentService.update(organization.getId(), departmentId, departmentName, sortOrder, operatorUserId);
    }

    @Override
    @Transactional
    public void deleteDepartment(String organizationNo, Long departmentId, Long operatorUserId) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        memberService.requireMember(organization.getId(), operatorUserId);
        departmentService.delete(
            organization.getId(),
            departmentId,
            memberService.existsByDepartment(departmentId),
            operatorUserId
        );
    }
}
