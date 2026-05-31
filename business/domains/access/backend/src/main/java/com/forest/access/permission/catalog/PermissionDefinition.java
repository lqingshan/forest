package com.forest.access.permission.catalog;

public record PermissionDefinition(
    String code,
    String name,
    String description,
    PermissionRiskLevel riskLevel,
    boolean grantable,
    int sortOrder,
    PermissionCatalog catalog
) {
}
