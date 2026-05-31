# User Lead 文档地图

User Lead 是用户侧线索展示、可见性、遮罩和解锁的聚合模块。

## 模块定位

`business/aggregations/user-lead` 组合 user、lead、point 等 domain 能力，为小程序用户提供线索列表、详情、已解锁线索和解锁流程。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 用户侧线索列表 | 基于 lead 搜索能力生成用户视角列表 |
| 用户侧详情 | 根据是否已解锁决定联系方式可见性 |
| 已解锁列表 | 查询当前 user 已解锁线索 |
| 解锁流程 | 校验积分、扣减积分、记录解锁事实 |
| 前端组件 | 小程序线索列表、详情和已解锁视图 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 线索主数据 CRUD | 属于 lead domain |
| 积分账户模型 | 属于 point domain |
| 支付充值 | 属于 recharge/payment |

## 关键技术点

- 用户侧业务主体是 `userId`，不是 `accountId`。
- lead domain 只提供线索基础事实和搜索能力；用户可见性由本 aggregation 处理。
- 解锁流程需要组合 lead、point 和用户上下文，避免把跨域流程写入 app 或单一 domain。
- 小程序页面负责交互和平台动作，不承担可信扣积分逻辑。

## 职责边界

| 层级 | 职责 |
|---|---|
| user-lead aggregation | 用户侧线索视图、遮罩和解锁编排 |
| lead domain | 线索主数据和基础搜索 |
| point domain | 积分余额和流水 |
| apps/trade-leads | 小程序页面壳和端流程 |

## 推荐阅读顺序

1. 本文档地图。
2. [../../../domains/lead/docs/README.md](../../../domains/lead/docs/README.md)：lead domain 文档地图。
3. [../../../domains/point/docs/README.md](../../../domains/point/docs/README.md)：point domain 文档地图。
4. [../../../../apps/trade-leads/docs/README.md](../../../../apps/trade-leads/docs/README.md)：Trade Leads app 文档地图。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| `api.md` | 用户侧线索接口说明 | 待补 |
| `unlock-flow.md` | 解锁扣积分流程和幂等说明 | 待补 |

## 当前状态

- 后端和小程序 frontend 模块已存在。
- 解锁流程和用户侧可见性规则需要继续补充详细文档。

## 维护规则

- 用户侧线索视图和解锁编排放在本模块。
- 线索基础模型和积分账户模型分别回到对应 domain。
