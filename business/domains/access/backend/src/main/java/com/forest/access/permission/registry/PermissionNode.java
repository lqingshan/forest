package com.forest.access.permission.registry;

import com.forest.access.permission.catalog.PermissionRiskLevel;

import java.util.List;

public record PermissionNode(
    String code,
    String name,
    String type,
    boolean grantable,
    PermissionRiskLevel riskLevel,
    int sortOrder,
    List<PermissionNode> children
) {
}
