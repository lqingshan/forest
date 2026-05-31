# 前端组件复用规范

本文档用于避免重复手写已有前端组件和状态能力。适用范围包括 `base-frontend`、`business/domains/*/frontend`、`business/aggregations/*/frontend` 和 `apps/*/clients` 内部已经沉淀的组件。

## 核心原则

前端开发默认先复用，后新增。

app client 应该优先做“路由、布局、导航、app 配置和业务组合”，不要重复实现公共 UI、领域 UI、聚合业务页面或已经存在的 app 内部组件。

开发任何前端页面或组件前，先按顺序检查：

1. 是否已有 `base-frontend` 通用组件能覆盖。
2. 这个 UI、状态或流程是否属于某个 domain。
3. 对应 `business/domains/<domain>/frontend` 是否已有组件、状态工厂或 API 封装。
4. 如果涉及多个 domain，是否已有 `business/aggregations/<name>/frontend` 组件。
5. 当前 app 内部是否已有相同页面片段或布局组件。
6. 只有确认没有可复用能力时，才新增组件。

## 复用优先级

| 优先级 | 位置 | 适用场景 |
|---|---|---|
| 1 | `base-frontend/packages/*` | 跨 app、无业务语义的基础 UI 和基础设施 |
| 2 | `business/domains/<domain>/frontend` | 单一业务域的组件、流程、状态和 API 封装 |
| 3 | `business/aggregations/<name>/frontend` | 多 domain 组合页面或业务编排组件 |
| 4 | `apps/<app>/clients/<client>` | 当前 app 独有的路由壳、导航、布局和轻量组合 |

如果组件带有明确业务语义，优先放在 domain 或 aggregation，而不是 base。

## 典型强制复用规则

| 场景 | 必须优先使用 | 禁止做法 |
|---|---|---|
| PC 登录页 | `@forest/user/web/auth` 的 `WebPcLoginFlow` | 在 app 页面手写手机号密码/验证码登录流程 |
| 当前登录用户展示 | `@forest/user/web/me` 的 `CurrentUserBadge` | 在 app layout 中手写 `currentUser.name/phone/avatarUrl` 展示 |
| 企业工作台状态 | `@forest/organization/web` 的 `createOrganizationWorkspaceState` | 每个 app 复制 `organization-state.ts` 的列表、权限、并发刷新逻辑 |
| 企业资料/认证/部门/员工工作台 | `@forest/organization/web` 的 workspace 组件 | 在 app 页面重复实现单 domain 的管理 UI |
| 角色权限基础 UI | `@forest/access/web` 的 role/permission/assignment 组件 | 在 aggregation 或 app 中重复写权限树、角色列表、授权主体列表 |
| 企业工作台角色权限页 | `@forest/organization-access` | 在 organization 或 access 单 domain 中写跨域页面 |

## 允许例外

只有以下情况可以不复用已有组件：

- 组件确实不满足当前业务，且改造会破坏已有使用方。
- 当前场景只是一次性迁移过渡，并有明确清理计划。
- 目标 UI 属于新 domain，还没有可复用组件。

例外必须在代码附近写短注释，说明为什么不复用已有组件。

## Review 检查项

审阅前端页面、layout、router、状态文件和组件时，必须检查：

- 是否已有 base/domain/aggregation/app 内部组件可以复用。
- 是否手写了登录流程，而没有使用 `WebPcLoginFlow`。
- 是否手写了当前用户头像、姓名、手机号展示，而没有使用 `CurrentUserBadge`。
- 是否复制了企业工作台 `organizations / selectedOrganizationNo / permissions / can()` 状态逻辑。
- 是否在 app 中实现了单 domain 的复杂 CRUD UI。
- 是否在 app 中组合了多个 domain，而更适合放到 aggregation frontend。
- 是否把可复用的局部 UI 留在页面内部，导致后续页面再次复制。

可用的快速搜索：

```bash
rg "currentUser\\?\\.(name|phone|avatarUrl)" apps -g '*.vue'
rg "selectedOrganizationNo|refreshOrganizations|fetchMyOrganizationPermissions" apps -g '*.ts'
rg "PhonePasswordLoginPanel|PhoneSmsLoginPanel" apps -g '*.vue'
```

搜索结果不是一定错误，但必须逐个确认是否应该改为复用领域组件。

## 组件地图

开发前先查对应模块的组件地图：

| 模块 | 组件地图 |
|---|---|
| base-frontend | [base-frontend/docs/frontend-components.md](../../base-frontend/docs/frontend-components.md) |
| user | [business/domains/user/docs/frontend-components.md](../../business/domains/user/docs/frontend-components.md) |
| organization | [business/domains/organization/docs/frontend-components.md](../../business/domains/organization/docs/frontend-components.md) |
| access | [business/domains/access/docs/frontend-components.md](../../business/domains/access/docs/frontend-components.md) |
| organization-access | [business/aggregations/organization-access/docs/frontend-components.md](../../business/aggregations/organization-access/docs/frontend-components.md) |
| attendance app | [apps/attendance/docs/frontend-components.md](../../apps/attendance/docs/frontend-components.md) |
| cxc-commerce app | [apps/cxc-commerce/docs/frontend-components.md](../../apps/cxc-commerce/docs/frontend-components.md) |

新增 base、domain、aggregation 或 app 内部可复用组件时，必须同步更新对应组件地图或模块文档。
