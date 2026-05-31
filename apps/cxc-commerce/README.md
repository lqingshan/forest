# CXC Commerce

CXC Commerce 是面向 CXC 客户的独立 B2B 多商家商城应用实例。

本应用实例拥有独立的用户中心、账号中心、认证会话和业务数据。通用能力仍复用 `business/domains/*` 和 `base-*`，`apps/cxc-commerce` 只承载 CXC 项目的应用装配、端应用入口、环境配置和项目级编排。

## 应用结构

```text
apps/cxc-commerce
  backend
  clients
    buyer-mobile-h5
    buyer-wechat-miniapp
    buyer-android-shell
    buyer-ios-shell
    merchant-web
    platform-web
  env
```

## 端应用

| 目录 | 端 | 职责 |
| --- | --- | --- |
| `backend` | 后端应用 | CXC 项目级装配、接口暴露、配置入口、认证与业务模块组合 |
| `clients/buyer-mobile-h5` | 用户端移动 H5 | 嵌入 Android / iOS，承载用户端移动业务页面和 H5 登录编排 |
| `clients/buyer-wechat-miniapp` | 用户端微信小程序 | 承载微信小程序用户端入口、微信直接登录、微信绑定手机号登录和用户端交易流程 |
| `clients/buyer-android-shell` | Android 原生壳 | 提供号码认证 SDK、微信 SDK、JSBridge、安全存储等原生能力 |
| `clients/buyer-ios-shell` | iOS 原生壳 | 提供号码认证 SDK、JSBridge、安全存储等原生能力，首版不接微信登录 |
| `clients/merchant-web` | 商家端 PC | 承载商家后台、商品、订单、履约、售后等商家工作台能力 |
| `clients/platform-web` | 平台端 PC | 承载平台后台、商家管理、类目、运营、订单监管、对账等平台能力 |

## 边界原则

- `apps/cxc-commerce` 是 CXC 应用装配层，不沉淀通用用户、账号、认证核心逻辑。
- 通用 `user / account / auth_session / login_log` 能力归属 `business/domains/user`。
- 业务域能力优先沉淀在 `business/domains/*`，跨域编排再进入 `business/aggregations/*`。
- Android / iOS shell 只提供原生能力，不承载业务规则。
- H5 / 小程序 / Web 前端只做交互、状态展示和接口调用，不自行实现可信认证判断。

## 数据库命名

CXC Commerce 使用独立 PostgreSQL database，不复用其他 app 的用户、账号、认证和业务数据。

```text
local: cxc_commerce_local
prod:  cxc_commerce_prod
```

首次运行前先创建空库，表结构由 CXC 后端启动后的 Flyway migration 管理。

```bash
./deploy/scripts/postgres-db.sh local create cxc-commerce
./deploy/scripts/postgres-db.sh local verify cxc-commerce

./deploy/scripts/postgres-db.sh prod-core create cxc-commerce
./deploy/scripts/postgres-db.sh prod-core verify cxc-commerce
```

## 项目文档

详细 APP 介绍见：

```text
apps/cxc-commerce/docs/overview.md
```
