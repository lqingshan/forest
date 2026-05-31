# Forest 文档地图

本文件是 Forest 的全局文档导航，只链接到各 app/module 自己的内部文档地图。

具体业务细节不在这里展开；需要了解某个模块时，先进入对应模块的 `docs/README.md`。

## 推荐阅读入口

| 文档 | 作用 |
|---|---|
| [docs/README.md](./README.md) | 根文档入口和全局文档维护规则 |
| [architecture/architecture.md](./architecture/architecture.md) | Forest 分层、app/domain/aggregation 边界 |
| [guidelines/development-guidelines.md](./guidelines/development-guidelines.md) | 统一开发规范 |
| [guidelines/frontend-component-reuse.md](./guidelines/frontend-component-reuse.md) | 前端组件复用、组件地图和 review 检查项 |
| [guidelines/database-guidelines.md](./guidelines/database-guidelines.md) | 数据库与 migration 规范 |
| [cicd/README.md](./cicd/README.md) | CI/CD 与质量门禁规范 |

## Apps

| App | 文档地图 | 定位 |
|---|---|---|
| Trade Leads | [apps/trade-leads/docs/README.md](../apps/trade-leads/docs/README.md) | 线索浏览、积分解锁、支付链路和小程序业务 |
| CXC Commerce | [apps/cxc-commerce/docs/README.md](../apps/cxc-commerce/docs/README.md) | B2B 多商家商城、商家后台和平台治理 |
| Attendance | [apps/attendance/docs/README.md](../apps/attendance/docs/README.md) | 考勤系统 app 骨架、平台端和企业后台端 |
| AI Content Generation | [apps/ai-content-generation/docs/README.md](../apps/ai-content-generation/docs/README.md) | AI 内容生成 app 空壳与后续规划 |
| Gateway | [apps/gateway/docs/README.md](../apps/gateway/docs/README.md) | Nginx gateway、反向代理和部署入口 |

## Business Domains

| Domain | 文档地图 | 定位 |
|---|---|---|
| Access | [business/domains/access/docs/README.md](../business/domains/access/docs/README.md) | RBAC、权限点、角色和授权关系 |
| File | [business/domains/file/docs/README.md](../business/domains/file/docs/README.md) | 文件上传下载、文件元数据和对象存储接入 |
| Lead | [business/domains/lead/docs/README.md](../business/domains/lead/docs/README.md) | 线索主数据、搜索和后台管理 |
| Notification | [business/domains/notification/docs/README.md](../business/domains/notification/docs/README.md) | 通知发送记录和短信审计 |
| Organization | [business/domains/organization/docs/README.md](../business/domains/organization/docs/README.md) | 企业、部门、员工、认证和企业工作台上下文 |
| Payment | [business/domains/payment/docs/README.md](../business/domains/payment/docs/README.md) | 支付单、支付渠道和回调处理 |
| Point | [business/domains/point/docs/README.md](../business/domains/point/docs/README.md) | 用户积分余额、积分流水和积分事件 |
| Recharge | [business/domains/recharge/docs/README.md](../business/domains/recharge/docs/README.md) | 充值套餐、充值订单和支付联动 |
| User | [business/domains/user/docs/README.md](../business/domains/user/docs/README.md) | 用户、账号、登录身份、session 和 token |
| Verification | [business/domains/verification/docs/README.md](../business/domains/verification/docs/README.md) | 验证码、校验票据、限流和冷却 |

## Business Aggregations

| Aggregation | 文档地图 | 定位 |
|---|---|---|
| Organization Access | [business/aggregations/organization-access/docs/README.md](../business/aggregations/organization-access/docs/README.md) | 企业工作台内的角色权限管理编排 |
| User Lead | [business/aggregations/user-lead/docs/README.md](../business/aggregations/user-lead/docs/README.md) | 用户侧线索展示、解锁和可见性编排 |
| User Point | [business/aggregations/user-point/docs/README.md](../business/aggregations/user-point/docs/README.md) | 用户积分查询和积分视图编排 |

## Base

| Base | 文档地图 | 定位 |
|---|---|---|
| Base Backend | [base-backend/docs/README.md](../base-backend/docs/README.md) | 后端 starter 和跨 app 基础设施 |
| Base Frontend | [base-frontend/docs/README.md](../base-frontend/docs/README.md) | 前端公共 packages 和跨 app 前端基础设施 |

## 维护规则

- 全局地图只链接模块地图，不直接展开模块内所有具体文档。
- app/module 的业务细节优先写入自己的 `docs/README.md` 或同级具体文档。
- 跨模块规范写入根 `docs/guidelines`、`docs/architecture`、`docs/database` 或 `docs/cicd`。
- 新增 app/domain/aggregation/base package 时，必须同步补充内部文档地图并在本文件登记。
