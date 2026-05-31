# Attendance 文档地图

Attendance 是独立考勤系统 app，一期目标是先建立平台端、企业后台端、登录准入、企业上下文和文档骨架。

## 模块定位

`apps/attendance` 是考勤系统运行 app。它只做 app 装配、端入口和环境配置，不沉淀通用考勤业务模型。

后续考勤规则、考勤记录、审批流等能力应进入独立 `business/domains/attendance`。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 后端 app 壳 | `apps/attendance/backend`，负责 Spring Boot 装配 |
| 企业后台端 | `clients/admin-web`，面向企业管理员、HR、员工主管 |
| 平台端 | `clients/platform-web`，面向平台运营/管理员 |
| 企业上下文 | 企业后台复用 organization workspace，接口带 `X-Organization-No` |
| 平台准入 | 平台端复用平台企业配置和 `PLATFORM:{boundaryId}` |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 移动端/小程序端 | 一期只做 Web PC |
| 考勤业务表 | 等 `business/domains/attendance` 启动后再设计 |
| 打卡流程 | 后续进入考勤 domain |
| 审批流 | 后续结合规则和组织架构再设计 |

## 关键技术点

- Admin-Web 使用 `ADMIN` token，企业工作台接口额外携带 `X-Organization-No`。
- 后端通过 `OrganizationWorkspaceInterceptor` 构建请求级企业上下文和 RBAC 边界。
- Platform-Web 使用 `PLATFORM` token，不使用 `X-Organization-No`。
- 平台端准入依赖 `forest.platform.organization-no` 和 `forest.platform.boundary-id`。
- 角色权限复用 `access` RBAC 和 `organization-access` aggregation。
- 前端页面壳必须优先复用 user、organization、access、organization-access 已有组件。

## 职责边界

| 层级 | 职责 |
|---|---|
| `apps/attendance/backend` | app 装配、拦截器注册、配置入口 |
| `apps/attendance/clients/admin-web` | 企业后台端页面壳和企业工作台入口 |
| `apps/attendance/clients/platform-web` | 平台端页面壳和平台监管入口 |
| `business/domains/organization` | 企业、员工、认证和 workspace 上下文 |
| `business/domains/access` | RBAC 权限点、角色和授权 |
| future `business/domains/attendance` | 考勤规则、记录、审批等核心业务 |

## 推荐阅读顺序

1. [overview.md](./overview.md)：考勤系统定位和一期范围。
2. [architecture.md](./architecture.md)：端、后端 app 和业务模块关系。
3. [clients.md](./clients.md)：admin-web 和 platform-web 端说明。
4. [frontend-components.md](./frontend-components.md)：Attendance 前端组件地图。
5. [runtime.md](./runtime.md)：运行和配置说明。
6. [development-plan.md](./development-plan.md)：后续考勤 domain 和业务建设路线。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| [overview.md](./overview.md) | 系统定位和一期范围 | 已有 |
| [architecture.md](./architecture.md) | 架构关系和依赖边界 | 已有 |
| [clients.md](./clients.md) | 前端端应用说明 | 已有 |
| [frontend-components.md](./frontend-components.md) | Attendance 前端组件地图 | 已有 |
| [runtime.md](./runtime.md) | 运行、代理和配置说明 | 已有 |
| [development-plan.md](./development-plan.md) | 后续开发计划 | 已有 |
| [requirements/README.md](./requirements/README.md) | 需求原始材料入口 | 已有 |
| [features/README.md](./features/README.md) | 功能方案入口 | 已有 |
| [decisions/README.md](./decisions/README.md) | 决策记录入口 | 已有 |

## 当前状态

- app 骨架、平台端、企业后台端和基础文档已建立。
- 考勤业务 domain 尚未启动。
- 后续新增考勤权限点时，应写入 access 权限目录，并同步更新本地图。

## 维护规则

- 考勤 app 自己的端划分、运行配置和产品范围写在本目录。
- 通用组织、权限、认证规则写入对应 business domain docs。
- 原始需求材料可以放入 `requirements/`，Word/PPT/PDF 也可以保留在该目录中。
- app 页面壳新增或调整时，同步检查 [frontend-components.md](./frontend-components.md) 和全局组件复用规范。
