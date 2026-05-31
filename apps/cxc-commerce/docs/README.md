# CXC Commerce 文档地图

CXC Commerce 是 CXC B2B 多商家商城 app，承载用户端、商家端、平台端和后端装配。

## 模块定位

`apps/cxc-commerce` 是面向 CXC 客户的独立应用实例。它拥有自己的 app 入口、端应用、环境配置和业务组合关系，但通用用户、组织、权限、支付等能力继续沉淀在 `business/domains/*`。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 商家端 PC | `merchant-web`，承载企业工作台、组织架构、角色权限等商家后台能力 |
| 平台端 PC | `platform-web`，承载平台企业准入和平台治理入口 |
| 用户端规划 | buyer H5、小程序、Android shell、iOS shell 的端边界已建立 |
| 多商家商城方案 | B2B 多商家商城业务架构和后续开发计划 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 完整交易闭环 | 商品、订单、履约、售后、结算仍待后续业务域建设 |
| 移动端完整实现 | 当前重点在 PC Web 和基础端壳 |
| app 内自建账号中心 | 账号、登录、session 复用 user domain |

## 关键技术点

- 商家端复用 organization workspace，请求通过 `X-Organization-No` 选择当前企业。
- 平台端复用 platform 准入配置，使用 `PLATFORM` token 和平台治理 RBAC 边界。
- 商家端角色权限复用 `access` domain 和 `organization-access` aggregation。
- CXC 是独立 app 实例，数据库、用户、账号、会话和业务数据按 app 隔离。
- Android/iOS shell 只提供原生能力，业务规则不下沉到原生壳。
- 前端页面壳必须优先复用 user、organization、access、organization-access 已有组件。

## 职责边界

| 层级 | 职责 |
|---|---|
| `apps/cxc-commerce/backend` | app 后端装配、接口暴露、配置入口 |
| `clients/merchant-web` | 商家后台 PC 端 |
| `clients/platform-web` | 平台后台 PC 端 |
| `clients/buyer-*` | 用户端 H5、小程序和原生壳 |
| `business/domains/organization` | 企业、员工、认证和工作台上下文 |
| `business/domains/access` | RBAC 权限点、角色和授权 |

## 推荐阅读顺序

1. [overview.md](./overview.md)：CXC Commerce app 定位、端范围和账号凭证边界。
2. [features/multi-merchant-mall-architecture.md](./features/multi-merchant-mall-architecture.md)：B2B 多商家商城业务架构方案。
3. [mobile-auth-design.md](./mobile-auth-design.md)：Android/iOS 原生壳 + H5 移动端登录方案。
4. [frontend-components.md](./frontend-components.md)：CXC Commerce 前端组件地图。
5. [development-plan.md](./development-plan.md)：后续开发计划。
6. [presentations/](./presentations/)：app 专属 PPT。

## 文档清单

| 文档 | 说明 |
|---|---|
| [overview.md](./overview.md) | CXC Commerce app 概览 |
| [mobile-auth-design.md](./mobile-auth-design.md) | CXC Android/iOS + H5 移动端登录方案 |
| [development-plan.md](./development-plan.md) | B2B 多商家商城开发计划 |
| [frontend-components.md](./frontend-components.md) | CXC Commerce 前端组件地图 |
| [features/multi-merchant-mall-architecture.md](./features/multi-merchant-mall-architecture.md) | B2B 多商家商城业务架构方案 |
| [presentations/](./presentations/) | app 专属 PPT |

## 当前状态

- app 结构、端目录、商家端和平台端基础能力正在建设。
- 组织、权限、企业工作台能力已经进入通用 business 模块沉淀。
- 商品、订单、履约、结算等电商核心业务域仍待后续补齐。

## 维护规则

- CXC 专属产品范围、端划分、客户隔离和电商方案写在本目录。
- 通用 organization、access、user、payment 规则写入对应 domain docs。
- 跨域编排如果多个 app 可复用，应优先沉淀到 `business/aggregations/*`。
- app 页面壳新增或调整时，同步检查 [frontend-components.md](./frontend-components.md) 和全局组件复用规范。
