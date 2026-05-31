package com.forest.access.permission.controller;

import com.forest.access.annotation.RequirePermission;
import com.forest.access.permission.catalog.AccessPermissionCodes;
import com.forest.access.permission.registry.PermissionNode;
import com.forest.access.permission.registry.PermissionRegistry;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端权限目录 API。
 *
 * <p>该 Controller 只暴露 access 模块自身的权限目录视图，不读取企业工作台上下文中的
 * organizationNo、certified、workspaceMode 等 organization 语义。路径仍挂在
 * {@code /api/admin/workspace/access} 下，是为了保持前端调用地址稳定。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.ADMIN + "/workspace/access")
public class AccessPermissionAdminController {
    private final PermissionRegistry permissionRegistry;

    public AccessPermissionAdminController(PermissionRegistry permissionRegistry) {
        this.permissionRegistry = permissionRegistry;
    }

    /**
     * 查询系统权限树。
     *
     * <p>用于角色授权页面展示权限目录。权限点定义来自代码内存视图，不从数据库读取。</p>
     */
    @GetMapping("/permission-tree")
    @RequirePermission(AccessPermissionCodes.ACCESS_ROLE_READ)
    public Result<List<PermissionNode>> permissionTree() {
        return Result.success(permissionRegistry.tree());
    }
}
