# Organization 文档地图

Organization 是企业、部门、员工、认证和企业工作台上下文领域模块。

## 模块定位

`business/domains/organization` 表达企业组织身份和企业工作台准入，不负责 RBAC 角色权限模型本身。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 企业 | 企业创建、企业资料、企业状态和认证状态 |
| 部门 | 企业内部门树和默认部门 |
| 员工 | `organization_member` 表达 user 在企业内的员工身份 |
| 企业认证 | 企业认证提交、审核状态和认证 Gate |
| 企业工作台 | `X-Organization-No` 选择当前企业，构建请求级 workspace 上下文 |
| 平台准入 | 通过配置的平台企业判断 platform-web 登录资格 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| RBAC 存储 | 角色、权限和授权关系归 access domain |
| 数据权限 | 一期不做部门范围、本人范围和字段级权限 |
| 考勤/商城业务 | 企业只是组织主体，不承载具体 app 业务规则 |

## 关键技术点

- `organization_member` 是权限主体和企业员工身份，不再用 `OWNER / ADMIN / MEMBER` 做业务判断。
- 企业工作台接口通过 `X-Organization-No` 声明当前企业，后端每次用当前 `userId + organizationNo` 校验 ACTIVE member。
- `OrganizationWorkspaceInterceptor` 构建 `OrganizationWorkspaceContext` 和 `AccessCheckContext`。
- `OrganizationWorkspaceAspect` 通过 `@RequireOrganizationFeature` / `@SkipOrganizationFeature` 执行企业认证 Gate。
- Platform 端准入使用 `forest.platform.organization-no`，平台治理边界使用 `forest.platform.boundary-id`。
- 企业工作台前端状态统一使用 `createOrganizationWorkspaceState`，不要在 app 复制企业列表、当前企业、权限刷新和 `can()` 逻辑。

## 职责边界

| 层级 | 职责 |
|---|---|
| organization domain | 企业、部门、员工、认证、workspace 上下文 |
| access domain | 权限点、角色、授权和 RBAC 校验 |
| organization-access aggregation | 企业工作台角色权限管理编排 |
| apps | 注册拦截器、提供端入口和页面壳 |

## 推荐阅读顺序

1. 本文档地图。
2. [workspace-guidelines.md](./workspace-guidelines.md)：企业工作台和认证 Gate 规范。
3. [frontend-components.md](./frontend-components.md)：organization 前端组件地图。
4. `backend/src/main/java/com/forest/organization/workspace`：workspace 上下文、Gate 和拦截器。
5. `backend/src/main/java/com/forest/organization/platform`：平台企业准入逻辑。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| [workspace-guidelines.md](./workspace-guidelines.md) | 企业工作台统一规范 | 已有 |
| [frontend-components.md](./frontend-components.md) | organization 前端状态、组件和复用规则 | 已有 |
| `api.md` | 企业、部门、员工、认证接口说明 | 待补 |
| `lifecycle.md` | 企业创建、认证、冻结和员工状态流转 | 待补 |

## 当前状态

- 企业、部门、员工、认证和 workspace 底座已建立。
- 企业工作台上下文已从 token 方案调整为请求头 + 后端校验方案。
- 角色权限管理接口已迁移到 organization-access aggregation。

## 维护规则

- 企业组织身份相关规则写在本模块。
- 跨 organization + access 的接口和页面编排写入 organization-access。
- app 业务不要反向污染 organization 模块。
- app 不要复制 organization 已提供的 workspace 状态和企业管理组件。
