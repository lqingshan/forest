# 企业工作台规范

本文档沉淀 organization、企业工作台上下文、企业认证 Gate 和当前企业准入规则。

相关文档：

- 总开发规范：[development-guidelines.md](../../../../docs/guidelines/development-guidelines.md)
- RBAC 规范：[rbac-guidelines.md](../../access/docs/rbac-guidelines.md)
- 架构边界：[architecture.md](../../../../docs/architecture/architecture.md)

## 1. 核心原则

| 规范 | 说明 |
|---|---|
| user 可属于多个企业 | `organization_member` 表达某个 user 在某个 organization 内的员工身份 |
| 当前工作台只有一个企业 | 前端一次只维护一个 `selectedOrganizationNo` |
| 创建企业不依赖企业上下文 | `POST /api/admin/organization` 只依赖普通 `ADMIN` token |
| 工作台接口依赖企业上下文 | `/api/admin/workspace/**` 必须带 `X-Organization-No` |
| 后端不信任 Header | 每次用 `currentUserId + organizationNo` 校验 ACTIVE member |
| 企业上下文不写入 JWT | auth/session 只保存 user、account、session、client、app、accessScope |
| 停用 member 不删除权限关系 | `DISABLED member` 不能进入工作台，重新启用后原角色关系继续生效 |

## 2. 请求链路

企业工作台接口统一链路：

```text
普通 ADMIN token
+ X-Organization-No
-> TokenAuthInterceptor 校验登录与 accessScope
-> OrganizationWorkspaceInterceptor 解析企业和 member
-> OrganizationWorkspaceContextHolder 写入当前请求上下文
-> AccessContextHolder 写入 ORGANIZATION:{organizationId} RBAC 边界
-> OrganizationWorkspaceAspect 执行企业认证 Gate
-> PermissionAspect 执行 RBAC 权限判断
-> Controller / Service
```

## 3. 组件职责

| 组件 | 职责 |
|---|---|
| `OrganizationWorkspaceInterceptor` | 读取 `X-Organization-No`，解析当前企业和 member，写入并清理 ThreadLocal |
| `OrganizationWorkspaceContextHolder` | 保存当前请求内的企业工作台上下文 |
| `CurrentOrganizationWorkspace` | 业务代码读取当前企业工作台上下文的注入入口 |
| `OrganizationWorkspaceAspect` | 只负责企业认证 Gate，不解析 Header，不写入 ThreadLocal |
| `AccessContextHolder` | 保存当前请求内的 RBAC 判断上下文 |

约束：

- 业务代码优先注入 `CurrentOrganizationWorkspace`，不要直接散用 `OrganizationWorkspaceContextHolder`。
- ThreadLocal 必须在请求完成后清理，避免复用线程污染后续请求。
- `OrganizationWorkspaceInterceptor` 不判断企业认证状态，认证状态由 `OrganizationWorkspaceAspect` 处理。

## 4. 企业认证 Gate

企业认证状态控制“后台功能是否可用”，不通过删除权限实现。

| 注解 | 说明 |
|---|---|
| `@RequireOrganizationFeature` | 默认要求企业认证通过 |
| `@RequireOrganizationFeature(allowUncertified = true)` | 未认证阶段也允许访问，例如企业资料、认证提交 |
| `@SkipOrganizationFeature` | 跳过企业认证 Gate，但不跳过登录、member 准入和 RBAC |

方法级注解优先于 Controller 级注解。

默认语义：

| 场景 | 行为 |
|---|---|
| 企业资料、认证提交 | `allowUncertified = true` |
| 部门、员工、角色权限 | 默认要求认证通过 |
| 未认证企业访问完整后台能力 | 企业认证 Gate 拦截 |
| 已认证企业访问业务接口 | 先通过企业 Gate，再进入 RBAC |

## 5. 企业入口接口

企业入口接口不属于企业工作台内部接口：

| 接口 | 说明 |
|---|---|
| `POST /api/admin/organization` | 创建企业，不依赖 `X-Organization-No` |
| `GET /api/admin/organization/my` | 查询当前 user 可进入的企业列表 |
| `POST /api/admin/organization/{organizationNo}/enter` | 选择企业，只返回企业工作台状态，不签发 workspace token |

企业切换列表规则：

| member 状态 | 是否展示 |
|---|---|
| `ACTIVE` | 展示 |
| `DISABLED` | 不展示 |
| `deleted = 1` | 不展示 |

## 6. 工作台路由

企业工作台业务接口统一使用：

```text
/api/admin/workspace/**
```

示例：

```text
/api/admin/workspace/organization
/api/admin/workspace/certification
/api/admin/workspace/department
/api/admin/workspace/member
/api/admin/workspace/access/**
/api/admin/workspace/attendance/**
```

不要通过 URL 中的 `{organizationNo}` 作为工作台业务接口的主方案。当前企业由 `X-Organization-No` 声明，并由后端每次校验。

## 7. 初始化规则

企业创建时初始化基础组织结构和 RBAC：

```text
创建 organization
-> 创建默认部门：默认部门
-> 创建 owner organization_member
-> 发布 OrganizationCreatedEvent
-> access 模块初始化默认角色与 owner 授权
```

约定：

- 企业创建不等待平台审核通过才初始化 RBAC。
- 平台审核通过只影响认证 Gate，不创建权限关系。
- RBAC 初始化失败时，企业创建整体回滚。
