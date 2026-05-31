# Organization Access 文档地图

Organization Access 是企业工作台内的角色权限管理聚合模块。

## 模块定位

`business/aggregations/organization-access` 组合 organization workspace 与 access RBAC，提供企业工作台内的角色、权限树、当前员工权限和员工角色分配能力。

它是跨域编排层，不拥有 organization 或 access 的核心实体与表。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 权限树查询 | 面向企业工作台授权页展示权限目录 |
| 我的权限 | 返回当前 member 在当前企业边界下的权限集合 |
| 角色管理 | 企业工作台内角色列表、详情、创建、更新、删除和权限替换 |
| 员工授权 | 查询和替换某个 organization_member 的角色集合 |
| 前端页面 | 企业工作台角色权限管理页和 access 组件组合 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 平台端角色管理 UI | 一期只覆盖企业工作台 |
| 数据权限 | 不做部门树、本人范围、字段级权限 |
| 权限点定义 | 权限点仍由 access domain 维护 |

## 关键技术点

- 接口挂在 `/api/admin/workspace/access/**`，依赖 `OrganizationWorkspaceInterceptor` 构建当前企业上下文。
- Controller 使用 `@RequireOrganizationFeature` 受企业认证 Gate 控制。
- RBAC 权限判断使用当前 `organization_member` 和 `ORGANIZATION:{organizationId}` 边界。
- `permission-tree` 只返回企业可授权目录，不暴露平台治理权限。
- 前端页面需要同时使用 workspace 状态和 `can(permissionCode)` 控制菜单、按钮和保存能力。
- `OrganizationAccessWorkspace` 是企业工作台角色权限管理的跨域页面，app 只做路由壳接入。

## 职责边界

| 层级 | 职责 |
|---|---|
| organization-access aggregation | 企业工作台内 access 相关跨域接口和页面 |
| organization domain | 当前企业上下文、member 校验、认证 Gate |
| access domain | 权限点、角色、权限模式和授权关系 |
| apps/*/admin-web | 路由接入、菜单接入和页面壳 |

## 推荐阅读顺序

1. 本文档地图。
2. [../../../domains/access/docs/README.md](../../../domains/access/docs/README.md)：access domain 文档地图。
3. [../../../domains/organization/docs/README.md](../../../domains/organization/docs/README.md)：organization domain 文档地图。
4. [frontend-components.md](./frontend-components.md)：organization-access 前端组件地图。
5. `backend/src/main/java/com/forest/organizationaccess/admin`：聚合接口和服务。
6. `frontend/src/web`：企业工作台角色权限页面。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| [frontend-components.md](./frontend-components.md) | organization-access 前端页面和复用规则 | 已有 |
| `api.md` | workspace access 接口说明 | 待补 |
| `frontend.md` | 角色权限页面交互说明 | 待补 |

## 当前状态

- 后端 aggregation 已建立。
- 前端 organization-access 页面正在建设。
- 后续补齐角色 CRUD 和员工授权接口后，应同步更新本地图。

## 维护规则

- 只放 organization + access 的跨域编排。
- 不在本模块新增 RBAC 存储表。
- 不把 app 专属菜单结构写入 access domain。
- app 不要重复实现 `OrganizationAccessWorkspace` 已承载的角色权限工作台。
