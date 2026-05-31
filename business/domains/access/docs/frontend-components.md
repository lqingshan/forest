# Access 前端组件地图

本文档登记 access domain frontend 已沉淀的 RBAC 基础组件。角色、权限点、权限模式和授权主体相关 UI 前，必须先查本地图。

## Permission Registry 展示

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `PermissionCatalogTree` | `@forest/access/web` | 只读展示权限目录树 | 不重复写权限目录浏览组件 |
| `PermissionNodeBadge` | `@forest/access/web` | 展示权限节点 code、风险、可授权状态 | 权限节点标签优先复用 |
| `PermissionPatternPreview` | `@forest/access/web` | 展示精确权限或通配符模式命中情况 | 不重复写 pattern 解释逻辑 |
| `PermissionCatalogFilter` | `@forest/access/web` | 权限目录过滤 | 不重复写 catalog 多选控件 |

## 角色权限模式

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `PermissionTreeSelector` | `@forest/access/web` | 多权限选择，支持精确权限和 grantable 通配符 | 权限树选择必须优先复用 |
| `PermissionPatternSelector` | `@forest/access/web` | 选择 `AccessRolePermissionPO.permission_pattern` 集合 | 角色授权 pattern 选择优先复用 |
| `PermissionPatternSummary` | `@forest/access/web` | 展示已选权限模式 | 不重复写已选权限摘要 |
| `RolePermissionEditor` | `@forest/access/web` | 角色权限编辑器 | 角色权限编辑页优先复用 |

## 角色基础信息

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `RoleListPanel` | `@forest/access/web` | 角色列表和新建入口 | 角色列表 UI 优先复用 |
| `RoleBasicEditor` | `@forest/access/web` | 角色名称、状态、保存、删除 | 角色基础信息编辑优先复用 |

## 角色授权关系

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `AssignmentSubjectList` | `@forest/access/web` | 展示通用授权主体列表 | 不写死 member，业务层映射成 subject |
| `RoleAssignmentEditor` | `@forest/access/web` | 给当前主体勾选角色 | 授权编辑优先复用 |
| `RoleAssignmentSummary` | `@forest/access/web` | 展示主体当前角色摘要 | 摘要展示优先复用 |
| `RoleAssignmentPanel` | `@forest/access/web` | 主体列表 + 角色分配组合面板 | 授权页面优先复用 |

## Review 检查

- 权限树、权限模式、角色列表、授权主体 UI 是否复用 access 组件。
- access 组件不应调用后端 API，不应感知 `organizationNo`、`memberId`、`X-Organization-No`。
- 业务页面负责 API、错误提示、删除确认、刷新策略和 `can()`。
- 企业工作台角色权限完整页面属于 organization-access aggregation，不属于 access 单 domain。
