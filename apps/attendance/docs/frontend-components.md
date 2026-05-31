# Attendance 前端组件地图

本文档登记 Attendance app 当前 app 层页面壳和可复用入口。app 层组件只承载端布局、路由入口和考勤专属占位，不重复实现 domain/aggregation 已有组件。

## Admin-Web

| 能力 | 位置 | 作用 | 复用/边界 |
|---|---|---|---|
| `WorkspaceLayout` | `clients/admin-web/src/layouts/WorkspaceLayout.vue` | 企业后台端布局、导航、当前用户入口 | 当前用户展示使用 `CurrentUserBadge` |
| `LoginPage` | `clients/admin-web/src/pages/LoginPage.vue` | ADMIN 登录页壳 | 登录流程使用 `WebPcLoginFlow` |
| `OrganizationsPage` | `clients/admin-web/src/pages/OrganizationsPage.vue` | 企业入口页壳 | 企业列表状态来自 `createOrganizationWorkspaceState` |
| `CertificationPage` | `clients/admin-web/src/pages/CertificationPage.vue` | 企业认证页壳 | 主体复用 `OrganizationCertificationWorkspace` |
| `AccessPage` | `clients/admin-web/src/pages/AccessPage.vue` | 角色权限页壳 | 主体复用 `OrganizationAccessWorkspace` |
| `AttendancePage` | `clients/admin-web/src/pages/AttendancePage.vue` | 考勤工作台占位 | 后续考勤 domain 建设后再替换 |

## Platform-Web

| 能力 | 位置 | 作用 | 复用/边界 |
|---|---|---|---|
| `WorkspaceLayout` | `clients/platform-web/src/layouts/WorkspaceLayout.vue` | 平台端布局和导航 | 不使用企业工作台 `X-Organization-No` |
| `LoginPage` | `clients/platform-web/src/pages/LoginPage.vue` | PLATFORM 登录页壳 | 登录流程使用 `WebPcLoginFlow` |
| `DashboardPage` | `clients/platform-web/src/pages/DashboardPage.vue` | 平台首页占位 | 后续平台指标组件再抽象 |
| `OrganizationOversightPage` | `clients/platform-web/src/pages/OrganizationOversightPage.vue` | 企业监管占位 | 平台企业治理能力成熟后再接入 |
| `AttendanceOversightPage` | `clients/platform-web/src/pages/AttendanceOversightPage.vue` | 考勤监管占位 | 后续考勤 domain 建设后再接入 |

## Review 检查

- Attendance app 不应复制 user、organization、access、organization-access 已有组件。
- 新增考勤业务组件前，先判断是否应该进入 future `business/domains/attendance/frontend`。
- app 层只保留端专属 layout、router、页面壳和配置。
