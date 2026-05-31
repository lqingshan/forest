# Organization Access 前端组件地图

本文档登记 organization-access aggregation frontend 已沉淀的跨域页面能力。企业工作台角色权限管理相关页面前，必须先查本地图。

## 跨域页面

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `OrganizationAccessWorkspace` | `@forest/organization-access/web` | 企业工作台内角色管理、权限授权、员工角色分配 | app 的 `/access` 页面只做路由壳和传参，不重复组合 access + organization 逻辑 |

## 边界说明

- organization-access 负责组合 organization workspace、access RBAC 和企业工作台页面交互。
- access domain 只提供通用 RBAC 组件，不读取 workspace。
- organization domain 只提供企业、部门、员工、认证和 workspace 状态能力。
- app client 只负责路由接入、菜单入口和 app 布局。

## Review 检查

- app 中是否重复实现了角色权限管理工作台。
- organization domain 中是否混入了 access 角色权限页面。
- access domain 中是否读取了 organization workspace 或 `X-Organization-No`。
- `OrganizationAccessWorkspace` 内部是否优先复用 `@forest/access/web` 基础组件。
