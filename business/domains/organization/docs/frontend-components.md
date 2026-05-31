# Organization 前端组件地图

本文档登记 organization domain frontend 已沉淀的可复用能力。app 开发企业工作台、企业资料、认证、部门、员工相关 UI 前，必须先查本地图。

## 企业工作台状态

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `createOrganizationWorkspaceState` | `@forest/organization/web` | admin/merchant 类企业工作台前端状态：企业列表、当前企业、权限集合、认证状态、`can()` | 禁止在 app 复制 `organization-state.ts` 的刷新、并发、localStorage 逻辑 |

app 侧只保留自己的 storage key 配置和导出壳：

```ts
const workspaceState = createOrganizationWorkspaceState({
  storageKey: 'forest.<app>.<client>.selectedOrganizationNo'
})
```

## 企业工作台 UI

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `OrganizationManagementWorkspace` | `@forest/organization/web` | 企业资料查看和编辑 | app 页面只传入当前 `organizationNo` |
| `OrganizationCertificationWorkspace` | `@forest/organization/web` | 企业认证提交和状态展示 | 未认证阶段入口优先复用 |
| `OrganizationDepartmentsWorkspace` | `@forest/organization/web` | 企业部门管理 | 不在 app 重复写部门 CRUD 页面 |
| `OrganizationMembersWorkspace` | `@forest/organization/web` | 企业员工管理 | 不在 app 重复写员工 CRUD 页面 |

## 平台端 UI

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `OrganizationCertificationReviewWorkspace` | `@forest/organization/platform-web` | 平台审核企业认证 | platform-web 审核页优先复用 |

## Review 检查

- app 是否复制了企业列表、当前企业、权限刷新或 `can()` 状态逻辑。
- 企业资料、认证、部门、员工页面是否复用 organization workspace 组件。
- 跨 organization + access 的角色权限页面是否放到 organization-access aggregation。
- 企业上下文仍应由后端 `X-Organization-No` + workspace interceptor 兜底，前端状态不是安全边界。
