package com.forest.organization.department.service;

import com.forest.organization.common.OrganizationNumberGenerator;
import com.forest.organization.department.entity.OrganizationDepartmentPO;
import com.forest.organization.department.repository.OrganizationDepartmentRepository;
import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Provides organization department capabilities and department ownership validation.
 */
@Service
public class OrganizationDepartmentService {
    private static final int ACTIVE_DELETED = 0;
    private static final String DEFAULT_DEPARTMENT_NAME = "默认部门";

    private final OrganizationDepartmentRepository departmentRepository;
    private final OrganizationNumberGenerator numberGenerator;

    public OrganizationDepartmentService(
        OrganizationDepartmentRepository departmentRepository,
        OrganizationNumberGenerator numberGenerator
    ) {
        this.departmentRepository = departmentRepository;
        this.numberGenerator = numberGenerator;
    }

    @Transactional
    public OrganizationDepartmentPO createDefaultRoot(Long organizationId, Long operatorUserId) {
        OrganizationDepartmentPO department = new OrganizationDepartmentPO();
        department.setDepartmentNo(numberGenerator.nextDepartmentNo());
        department.setOrganizationId(organizationId);
        department.setParentId(null);
        department.setDepartmentName(DEFAULT_DEPARTMENT_NAME);
        department.setDefaultDepartment(true);
        department.setSortOrder(0);
        department.setStatus(OrganizationDepartmentPO.Status.ACTIVE);
        department.setCreatedId(operatorUserId);
        department.setModifiedId(operatorUserId);
        return departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDepartmentPO> list(Long organizationId) {
        return departmentRepository.findByOrganizationIdAndDeletedOrderBySortOrderAscIdAsc(organizationId, ACTIVE_DELETED);
    }

    @Transactional(readOnly = true)
    public OrganizationDepartmentPO requireDefaultDepartment(Long organizationId) {
        return departmentRepository.findByOrganizationIdAndDefaultDepartmentAndDeleted(organizationId, true, ACTIVE_DELETED)
            .orElseThrow(() -> new BusinessException("企业默认部门不存在"));
    }

    @Transactional(readOnly = true)
    public OrganizationDepartmentPO requireBelongsToOrganization(Long organizationId, Long departmentId) {
        if (departmentId == null) {
            return requireDefaultDepartment(organizationId);
        }
        OrganizationDepartmentPO department = departmentRepository
            .findByIdAndOrganizationIdAndDeleted(departmentId, organizationId, ACTIVE_DELETED)
            .orElseThrow(() -> new BusinessException("部门不存在"));
        if (department.getStatus() != OrganizationDepartmentPO.Status.ACTIVE) {
            throw new BusinessException("部门已停用");
        }
        return department;
    }

    @Transactional
    public OrganizationDepartmentPO create(
        Long organizationId,
        Long parentId,
        String departmentName,
        Integer sortOrder,
        Long operatorUserId
    ) {
        OrganizationDepartmentPO parent = parentId == null
            ? requireDefaultDepartment(organizationId)
            : requireBelongsToOrganization(organizationId, parentId);
        OrganizationDepartmentPO department = new OrganizationDepartmentPO();
        department.setDepartmentNo(numberGenerator.nextDepartmentNo());
        department.setOrganizationId(organizationId);
        department.setParentId(parent.getId());
        department.setDepartmentName(requireText(departmentName, "部门名称不能为空"));
        department.setDefaultDepartment(false);
        department.setSortOrder(sortOrder == null ? 0 : sortOrder);
        department.setStatus(OrganizationDepartmentPO.Status.ACTIVE);
        department.setCreatedId(operatorUserId);
        department.setModifiedId(operatorUserId);
        return departmentRepository.save(department);
    }

    @Transactional
    public OrganizationDepartmentPO update(
        Long organizationId,
        Long departmentId,
        String departmentName,
        Integer sortOrder,
        Long operatorUserId
    ) {
        OrganizationDepartmentPO department = requireBelongsToOrganization(organizationId, departmentId);
        department.setDepartmentName(requireText(departmentName, "部门名称不能为空"));
        if (sortOrder != null) {
            department.setSortOrder(sortOrder);
        }
        department.setModifiedId(operatorUserId);
        return departmentRepository.save(department);
    }

    @Transactional
    public void delete(Long organizationId, Long departmentId, boolean hasMembers, Long operatorUserId) {
        OrganizationDepartmentPO department = requireBelongsToOrganization(organizationId, departmentId);
        if (Boolean.TRUE.equals(department.getDefaultDepartment())) {
            throw new BusinessException("默认部门不能删除");
        }
        if (departmentRepository.existsByParentIdAndDeleted(department.getId(), ACTIVE_DELETED)) {
            throw new BusinessException("存在子部门，不能删除");
        }
        if (hasMembers) {
            throw new BusinessException("部门下存在员工，不能删除");
        }
        department.setDeleted(1);
        department.setModifiedId(operatorUserId);
        departmentRepository.save(department);
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }
}
