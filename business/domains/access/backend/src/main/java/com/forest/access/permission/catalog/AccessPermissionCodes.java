package com.forest.access.permission.catalog;

/**
 * Compile-time permission constants used by annotations and RBAC grants.
 */
public final class AccessPermissionCodes {
    public static final String ORGANIZATION_READ = "organization.read";
    public static final String ORGANIZATION_UPDATE = "organization.update";
    public static final String ORGANIZATION_CERTIFICATION_SUBMIT = "organization.certification.submit";
    public static final String ORGANIZATION_DEPARTMENT_READ = "organization.department.read";
    public static final String ORGANIZATION_DEPARTMENT_CREATE = "organization.department.create";
    public static final String ORGANIZATION_DEPARTMENT_UPDATE = "organization.department.update";
    public static final String ORGANIZATION_DEPARTMENT_DELETE = "organization.department.delete";
    public static final String ORGANIZATION_MEMBER_READ = "organization.member.read";
    public static final String ORGANIZATION_MEMBER_CREATE = "organization.member.create";
    public static final String ORGANIZATION_MEMBER_UPDATE = "organization.member.update";
    public static final String ORGANIZATION_MEMBER_DISABLE = "organization.member.disable";
    public static final String ORGANIZATION_MEMBER_ACTIVATE = "organization.member.activate";

    public static final String ACCESS_ROLE_READ = "access.role.read";
    public static final String ACCESS_ROLE_CREATE = "access.role.create";
    public static final String ACCESS_ROLE_UPDATE = "access.role.update";
    public static final String ACCESS_ROLE_DELETE = "access.role.delete";
    public static final String ACCESS_ASSIGNMENT_READ = "access.assignment.read";
    public static final String ACCESS_ASSIGNMENT_MANAGE = "access.assignment.manage";

    public static final String PLATFORM_ORGANIZATION_READ = "platform.organization.read";
    public static final String PLATFORM_ORGANIZATION_STATUS_UPDATE = "platform.organization.status.update";
    public static final String PLATFORM_ORGANIZATION_CERTIFICATION_READ = "platform.organization.certification.read";
    public static final String PLATFORM_ORGANIZATION_CERTIFICATION_REVIEW = "platform.organization.certification.review";

    private AccessPermissionCodes() {
    }
}
