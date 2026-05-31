package com.forest.access.permission.catalog;

import java.util.List;

/**
 * The single code-side source of system permission definitions.
 */
public final class AccessPermissionDefinitions {
    private AccessPermissionDefinitions() {
    }

    public static List<PermissionDefinition> permissions() {
        return List.of(
            permission(AccessPermissionCodes.ORGANIZATION_READ, "查看企业信息", PermissionRiskLevel.LOW, 100),
            permission(AccessPermissionCodes.ORGANIZATION_UPDATE, "编辑企业信息", PermissionRiskLevel.MEDIUM, 110),
            permission(AccessPermissionCodes.ORGANIZATION_CERTIFICATION_SUBMIT, "提交企业认证", PermissionRiskLevel.MEDIUM, 120),
            permission(AccessPermissionCodes.ORGANIZATION_DEPARTMENT_READ, "查看部门", PermissionRiskLevel.LOW, 200),
            permission(AccessPermissionCodes.ORGANIZATION_DEPARTMENT_CREATE, "新增部门", PermissionRiskLevel.MEDIUM, 210),
            permission(AccessPermissionCodes.ORGANIZATION_DEPARTMENT_UPDATE, "编辑部门", PermissionRiskLevel.MEDIUM, 220),
            permission(AccessPermissionCodes.ORGANIZATION_DEPARTMENT_DELETE, "删除部门", PermissionRiskLevel.HIGH, 230),
            permission(AccessPermissionCodes.ORGANIZATION_MEMBER_READ, "查看员工", PermissionRiskLevel.LOW, 300),
            permission(AccessPermissionCodes.ORGANIZATION_MEMBER_CREATE, "新增员工", PermissionRiskLevel.MEDIUM, 310),
            permission(AccessPermissionCodes.ORGANIZATION_MEMBER_UPDATE, "编辑员工", PermissionRiskLevel.MEDIUM, 320),
            permission(AccessPermissionCodes.ORGANIZATION_MEMBER_DISABLE, "停用员工", PermissionRiskLevel.HIGH, 330),
            permission(AccessPermissionCodes.ORGANIZATION_MEMBER_ACTIVATE, "启用员工", PermissionRiskLevel.HIGH, 340),
            access(AccessPermissionCodes.ACCESS_ROLE_READ, "查看角色", PermissionRiskLevel.LOW, 400),
            access(AccessPermissionCodes.ACCESS_ROLE_CREATE, "新增角色", PermissionRiskLevel.HIGH, 410),
            access(AccessPermissionCodes.ACCESS_ROLE_UPDATE, "编辑角色", PermissionRiskLevel.HIGH, 420),
            access(AccessPermissionCodes.ACCESS_ROLE_DELETE, "删除角色", PermissionRiskLevel.CRITICAL, 430),
            access(AccessPermissionCodes.ACCESS_ASSIGNMENT_READ, "查看员工角色", PermissionRiskLevel.LOW, 440),
            access(AccessPermissionCodes.ACCESS_ASSIGNMENT_MANAGE, "分配员工角色", PermissionRiskLevel.CRITICAL, 450),
            platform(AccessPermissionCodes.PLATFORM_ORGANIZATION_READ, "平台查看企业", PermissionRiskLevel.LOW, 500),
            platform(AccessPermissionCodes.PLATFORM_ORGANIZATION_STATUS_UPDATE, "平台修改企业状态", PermissionRiskLevel.CRITICAL, 510),
            platform(AccessPermissionCodes.PLATFORM_ORGANIZATION_CERTIFICATION_READ, "查看认证申请", PermissionRiskLevel.LOW, 520),
            platform(AccessPermissionCodes.PLATFORM_ORGANIZATION_CERTIFICATION_REVIEW, "审核认证申请", PermissionRiskLevel.HIGH, 530)
        );
    }

    public static List<PermissionPrefixDefinition> prefixes() {
        return List.of(
            new PermissionPrefixDefinition("organization", "组织管理", 100, true),
            new PermissionPrefixDefinition("organization.certification", "企业认证", 120, true),
            new PermissionPrefixDefinition("organization.department", "部门管理", 200, true),
            new PermissionPrefixDefinition("organization.member", "员工管理", 300, true),
            new PermissionPrefixDefinition("access", "角色权限", 400, true),
            new PermissionPrefixDefinition("access.role", "角色管理", 410, true),
            new PermissionPrefixDefinition("access.assignment", "角色分配", 440, true),
            new PermissionPrefixDefinition("platform", "平台治理", 500, true),
            new PermissionPrefixDefinition("platform.organization", "企业治理", 510, true),
            new PermissionPrefixDefinition("platform.organization.certification", "企业认证审核", 520, true),
            new PermissionPrefixDefinition("platform.organization.status", "企业状态", 530, true)
        );
    }

    private static PermissionDefinition permission(String code, String name, PermissionRiskLevel riskLevel, int sortOrder) {
        return new PermissionDefinition(code, name, name, riskLevel, true, sortOrder, PermissionCatalog.ORGANIZATION_SELF);
    }

    private static PermissionDefinition access(String code, String name, PermissionRiskLevel riskLevel, int sortOrder) {
        return new PermissionDefinition(code, name, name, riskLevel, true, sortOrder, PermissionCatalog.ORGANIZATION_ACCESS);
    }

    private static PermissionDefinition platform(String code, String name, PermissionRiskLevel riskLevel, int sortOrder) {
        return new PermissionDefinition(code, name, name, riskLevel, true, sortOrder, PermissionCatalog.PLATFORM_GOVERNANCE);
    }
}
