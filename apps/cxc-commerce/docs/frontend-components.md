# CXC Commerce 前端组件地图

本文档登记 CXC Commerce app 当前 app 层页面壳和可复用入口。app 层只承载 CXC 专属端入口、路由、布局和电商业务壳，不重复实现通用 domain/aggregation 组件。

## Merchant-Web

| 能力 | 位置 | 作用 | 复用/边界 |
|---|---|---|---|
| `WorkspaceLayout` | `clients/merchant-web/src/layouts/WorkspaceLayout.vue` | 商家后台布局、导航、当前用户入口 | 当前用户展示应使用 `CurrentUserBadge` |
| `LoginPage` | `clients/merchant-web/src/pages/LoginPage.vue` | ADMIN 登录页壳 | 登录流程应使用 `WebPcLoginFlow` |
| `OrganizationsPage` | `clients/merchant-web/src/pages/OrganizationsPage.vue` | 企业入口页壳 | 企业状态来自 `createOrganizationWorkspaceState` |
| `CertificationPage` | `clients/merchant-web/src/pages/CertificationPage.vue` | 企业认证页壳 | 主体复用 `OrganizationCertificationWorkspace` |
| `DepartmentsPage` | `clients/merchant-web/src/pages/DepartmentsPage.vue` | 部门管理页壳 | 主体复用 `OrganizationDepartmentsWorkspace` |
| `MembersPage` | `clients/merchant-web/src/pages/MembersPage.vue` | 员工管理页壳 | 主体复用 `OrganizationMembersWorkspace` |
| `AccessPage` | `clients/merchant-web/src/pages/AccessPage.vue` | 角色权限页壳 | 主体复用 `OrganizationAccessWorkspace` |

## Platform-Web

| 能力 | 位置 | 作用 | 复用/边界 |
|---|---|---|---|
| `WorkspaceLayout` | `clients/platform-web/src/layouts/WorkspaceLayout.vue` | 平台后台布局和导航 | 平台端不使用企业工作台 `X-Organization-No` |
| `LoginPage` | `clients/platform-web/src/pages/LoginPage.vue` | PLATFORM 登录页壳 | 登录流程应使用 `WebPcLoginFlow` |
| `CertificationReviewPage` | `clients/platform-web/src/pages/CertificationReviewPage.vue` | 企业认证审核页壳 | 主体复用 `OrganizationCertificationReviewWorkspace` |

## Buyer 端

| 能力 | 位置 | 作用 | 复用/边界 |
|---|---|---|---|
| `buyer-wechat-miniapp` | `clients/buyer-wechat-miniapp` | 买家小程序端壳 | 小程序基础能力优先复用 base/user miniapp 模块 |

## Review 检查

- CXC app 不应复制 user、organization、access、organization-access 已有组件。
- 电商业务组件如果可跨 app 复用，应沉淀到后续 commerce 相关 domain/aggregation frontend。
- app 层只保留 CXC 专属 layout、router、页面壳和配置。
