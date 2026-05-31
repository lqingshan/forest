package com.forest.access.permission.check;

import com.forest.access.core.AccessCheckContext;

/**
 * 权限判断契约。
 *
 * <p>面向 AOP、Controller 等调用方暴露最小能力：判断某个权限上下文是否拥有指定权限点。
 * 角色、授权关系、通配符展开、缓存等实现细节由具体实现隐藏。</p>
 */
public interface PermissionChecker {
    boolean hasPermission(AccessCheckContext context, String permissionCode);
}
