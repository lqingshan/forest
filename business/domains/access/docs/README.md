# Access 文档地图

Access 是 Forest 的 RBAC 领域模块，负责权限点、角色、角色权限和员工授权关系。

## 模块定位

`business/domains/access` 提供通用权限底座。它不读取具体 app URL，不理解企业工作台路由，也不直接决定当前请求属于哪个企业；这些上下文由 app 或 organization workspace 构建后传入。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 权限点定义 | 代码内维护 `PermissionDefinition`，启动时构建内存视图 |
| 权限注解 | `@RequirePermission`、`@RequireAllPermissions`、`@RequireAnyPermission` |
| 角色模型 | `access_role`、`access_role_permission`、`access_role_assignment` |
| 企业默认角色 | 企业创建后初始化企业所有者、管理员、普通员工角色 |
| 平台治理角色 | 支持 `PLATFORM:{boundaryId}` 边界下的平台治理权限 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 数据权限 | 一期不做部门树、本人范围、字段级权限 |
| ABAC 表达式 | 不引入复杂规则引擎 |
| app 专属菜单 | 前端菜单由具体 app/aggregation 结合权限集合实现 |

## 关键技术点

- 权限点全局共用，权限定义不入 DB，启动时构建 `codeSet`、`codeMap`、`permissionTree`、`wildcardIndex` 等内存视图。
- 接口权限通过注解声明；没有权限注解的接口默认不做 RBAC 判断。
- `AccessCheckContext` 表达权限主体和权限边界，例如 `ORGANIZATION_MEMBER + ORGANIZATION:{organizationId}`。
- `AccessContextHolder` 只保存当前请求的 RBAC 上下文，不保存企业完整业务上下文。
- 通配符只用于角色授权，例如 `organization.member.*`，不用于接口注解和前端 `can()`。
- access frontend 提供权限树、权限模式、角色列表和授权主体基础组件；业务页面应优先复用，不直接复制 UI 逻辑。

## 职责边界

| 层级 | 职责 |
|---|---|
| access domain | RBAC 模型、权限校验、角色和授权服务 |
| organization domain | 企业、员工、workspace 上下文和企业认证 Gate |
| organization-access aggregation | 企业工作台内的角色权限管理接口和页面编排 |
| apps | 注册拦截器、暴露端入口、组合页面 |

## 推荐阅读顺序

1. 本文档地图。
2. [rbac-guidelines.md](./rbac-guidelines.md)：RBAC 设计规范。
3. [frontend-components.md](./frontend-components.md)：access 前端组件地图。
4. [../backend/src/main/java/com/forest/access/permission/registry/PermissionRegistry.java](../backend/src/main/java/com/forest/access/permission/registry/PermissionRegistry.java)：权限内存视图。
5. [../backend/src/main/java/com/forest/access/role/service/AccessControlService.java](../backend/src/main/java/com/forest/access/role/service/AccessControlService.java)：权限校验和初始化服务。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| [rbac-guidelines.md](./rbac-guidelines.md) | RBAC 统一设计规范 | 已有 |
| [frontend-components.md](./frontend-components.md) | access 前端组件和复用规则 | 已有 |
| `api.md` | 角色管理与授权接口说明 | 待补 |
| `permission-catalog.md` | 权限点目录说明 | 待补 |

## 当前状态

- 后端 RBAC 底座已建立，角色权限管理能力正在补齐。
- 前端 access 组件和 organization-access 页面仍在建设中。
- 数据权限暂不做。

## 维护规则

- 通用权限模型和校验规则写在 access domain。
- 企业工作台内的角色权限页面和跨域接口写在 organization-access aggregation。
- 新增权限点必须更新权限定义、前端权限展示和对应接口注解。
- app 和 aggregation 不要重复实现 access 已提供的 RBAC 基础组件。
