# Trade Leads 文档地图

Trade Leads 是线索交易 app，主要承载微信小程序用户侧线索浏览、积分解锁、充值支付和后台基础运营能力。

## 模块定位

`apps/trade-leads` 是可运行 app。它负责装配 `user`、`lead`、`point`、`recharge`、`payment` 等业务域，并通过 aggregation 承载用户侧线索和积分视图。

app 层不拥有通用用户、账号、积分、线索、支付等核心模型；这些能力应继续沉淀到 `business/domains/*` 或 `business/aggregations/*`。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 微信小程序用户端 | 线索列表、线索详情、已解锁线索、积分流水、充值和支付结果 |
| 线索解锁 | 用户消耗积分解锁联系方式，解锁记录按 `userId` 归属 |
| 积分体系 | 积分余额和积分流水按 `userId` 归属，不归属 `accountId` |
| 充值支付 | 充值订单、微信小程序支付参数、支付回调和支付验收 |
| 后台能力 | 用户、线索、积分等基础后台管理能力由对应 domain 提供 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 多企业工作台 | Trade Leads 当前不是 organization workspace 型 app |
| 商家/店铺模型 | 不在当前线索交易 app 范围内 |
| 复杂 RBAC UI | 后续如需要平台权限治理，再接入 access 能力 |

## 关键技术点

- `userId` 是业务资产主体；解锁记录、积分余额、积分流水都不应归属到 `accountId`。
- 登录链路遵循 `identity/account -> user -> token`，账号只解决登录身份，业务流程以 `user` 为核心。
- `user-lead` aggregation 组合 `user + lead + point`，承载用户侧线索展示、遮罩和解锁流程。
- `user-point` aggregation 承载用户侧积分视图，避免 app 直接拼装 point domain 内部细节。
- 微信小程序支付由 `payment` 和 `recharge` 协作，app 小程序只负责支付调起时机和页面反馈。

## 职责边界

| 层级 | 职责 |
|---|---|
| `apps/trade-leads` | app 装配、运行配置、小程序页面壳、部署入口 |
| `business/domains/lead` | 线索主数据、搜索和后台管理 |
| `business/domains/point` | 积分余额、积分流水和积分事件 |
| `business/domains/recharge` | 充值套餐和充值订单 |
| `business/domains/payment` | 支付单、支付渠道和回调处理 |
| `business/aggregations/user-lead` | 用户侧线索可见性和解锁编排 |
| `business/aggregations/user-point` | 用户侧积分查询编排 |

## 推荐阅读顺序

1. 本文档地图。
2. [diagrams/architecture/](./diagrams/architecture/)：现有架构图和模块关系图。
3. [features/payment/wechat-miniapp-payment-test.md](./features/payment/wechat-miniapp-payment-test.md)：微信小程序支付链路测试清单。
4. [../../README.md](../README.md)：app 运行、部署和环境说明。

## 文档清单

| 文档 | 说明 |
|---|---|
| [diagrams/architecture/](./diagrams/architecture/) | Trade Leads 架构图 |
| [features/payment/wechat-miniapp-payment-test.md](./features/payment/wechat-miniapp-payment-test.md) | 微信小程序支付链路测试清单 |

## 当前状态

- 已有小程序、后端、gateway 和部署脚本。
- 已有支付验收清单和架构图资产。
- 仍缺少完整 `architecture.md`、需求规格和后台运营说明，后续应从本文档地图继续拆分。

## 维护规则

- Trade Leads 专属需求、支付验收和部署说明放在本 app docs。
- 通用 user、lead、point、recharge、payment 规则写入对应 domain docs。
- 跨 domain 用户侧线索或积分编排写入对应 aggregation docs。
