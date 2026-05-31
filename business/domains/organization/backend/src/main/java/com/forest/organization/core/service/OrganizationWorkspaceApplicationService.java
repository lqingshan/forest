package com.forest.organization.core.service;

import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.department.entity.OrganizationDepartmentPO;

import java.util.List;

/**
 * 企业工作台用例服务。
 *
 * <p>这里处理“已经选择某个企业之后”的企业内管理动作。调用方通常来自
 * {@code /api/admin/workspace/**}，当前企业由工作台拦截器解析。</p>
 */
public interface OrganizationWorkspaceApplicationService {
    /**
     * 查看当前企业详情。
     */
    OrganizationPO getOrganization(String organizationNo, Long operatorUserId);

    /**
     * 修改当前企业基础资料。
     */
    OrganizationPO updateOrganization(String organizationNo, String organizationName, Long operatorUserId);

    /**
     * 查询当前企业部门列表。
     */
    List<OrganizationDepartmentPO> listDepartments(String organizationNo, Long operatorUserId);

    /**
     * 创建当前企业部门。
     */
    OrganizationDepartmentPO createDepartment(
        String organizationNo,
        Long parentId,
        String departmentName,
        Integer sortOrder,
        Long operatorUserId
    );

    /**
     * 修改当前企业部门。
     */
    OrganizationDepartmentPO updateDepartment(
        String organizationNo,
        Long departmentId,
        String departmentName,
        Integer sortOrder,
        Long operatorUserId
    );

    /**
     * 删除当前企业部门。
     */
    void deleteDepartment(String organizationNo, Long departmentId, Long operatorUserId);
}
