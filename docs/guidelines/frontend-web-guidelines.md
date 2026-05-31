# Web 前端规范

本文档沉淀 PC Web app、企业工作台页面、权限展示和前端状态管理规则。小程序专项规则仍放在 [development-guidelines.md](./development-guidelines.md) 的微信小程序章节。

相关文档：

- 总开发规范：[development-guidelines.md](./development-guidelines.md)
- 组件复用规范：[frontend-component-reuse.md](./frontend-component-reuse.md)
- 企业工作台规范：[workspace-guidelines.md](../../business/domains/organization/docs/workspace-guidelines.md)
- RBAC 规范：[rbac-guidelines.md](../../business/domains/access/docs/rbac-guidelines.md)

## 1. app 页面壳

app 页面壳要薄。

| 场景 | 放置位置 |
|---|---|
| 路由页面、布局、当前 app 导航 | `apps/<app>/clients/<client>` |
| 单 domain 的 API、类型、组件 | `business/domains/<domain>/frontend` |
| 多 domain 聚合页面 | `business/aggregations/<name>/frontend` |
| 跨 app UI 基础设施 | `base-frontend/packages/*` |

示例：

- `AccessPage.vue` 属于 app，因为它只是 `/access` 路由页壳。
- `OrganizationAccessWorkspace.vue` 属于 `organization-access` aggregation，因为它组合 organization 工作台上下文和 access RBAC。

## 2. 组件复用

前端开发默认先复用，后新增。

规则：

- 写页面前先查 [frontend-component-reuse.md](./frontend-component-reuse.md) 和对应模块组件地图。
- 基础 UI 优先查 `base-frontend`。
- 单 domain UI、状态和流程优先查 `business/domains/<domain>/frontend`。
- 跨 domain 页面优先查 `business/aggregations/<name>/frontend`。
- app 内部已有页面片段或布局组件，也应优先复用。
- 如果已有组件不满足，优先改造原组件或抽象新组件，不要在页面里复制一份。

典型强制项：

- PC 登录页优先使用 `WebPcLoginFlow`。
- 当前登录用户展示优先使用 `CurrentUserBadge`。
- 企业工作台状态优先使用 `createOrganizationWorkspaceState`。
- 权限树、角色列表和授权主体选择优先使用 `@forest/access/web` 组件。

## 3. 企业工作台状态

企业后台端前端集中维护：

| 状态 | 说明 |
|---|---|
| `organizations` | 当前 user 可进入的 ACTIVE 企业列表 |
| `selectedOrganizationNo` | 当前选择的企业 |
| `permissions` | 当前企业下的精确权限集合 |
| `selectedOrganizationCertified` | 当前企业是否认证通过 |

规则：

- 切换企业后必须刷新 workspace 状态和权限集合。
- 退出登录或 token restore 失败时必须清理企业状态。
- 创建企业后需要 force refresh 企业列表。
- 后端仍会在每次 workspace 请求中根据 `X-Organization-No` 重新校验当前 user 是否是 ACTIVE member。
- 通用企业工作台状态必须使用 `@forest/organization/web` 的 `createOrganizationWorkspaceState`。

## 4. 权限展示

前端同时使用企业状态和权限集合：

```text
企业状态允许
+
can(permissionCode)
```

规则：

- 前端 `can()` 只判断精确权限点，不支持通配符。
- 通配符由后端角色授权展开为精确权限集合。
- 未认证企业只展示企业资料和认证相关入口。
- 认证通过后再展示部门、员工、角色权限、考勤等完整后台入口。
- 菜单和按钮都需要做权限控制；菜单隐藏不等于接口放行。

示例：

```ts
can('organization.member.create')
```

含义：

- 当前企业已选中。
- 当前企业认证状态允许访问对应 feature。
- 当前 member 拥有后端返回的精确权限点。

## 5. 登录组件

Web PC 登录页优先使用：

```text
WebPcLoginFlow
```

支持：

- 手机号密码登录。
- 手机验证码登录。

如果某个端只允许单一登录方式，再使用单一 panel。

每个 app client 必须使用独立 storage prefix，例如：

```text
forest.attendance.admin
forest.attendance.platform
```

## 6. 路由守卫

私有路由守卫负责：

- 校验 token 是否存在。
- restore 当前 user session。
- restore 失败时清理 app 业务状态。
- 企业后台端初始化企业列表。
- 按企业认证状态和权限点拦截页面。

路由守卫不负责可信业务判断；后端仍必须通过 interceptor、aspect、RBAC 注解兜底。

## 7. 构建验证

Web PC 改动后至少执行对应 package build：

```bash
pnpm --filter @forest/<app-client> build
```

涉及 shared frontend 包时，也应构建对应 business / aggregation 包。
