# Point 文档地图

Point 是用户积分余额、积分流水和积分事件领域模块。

## 模块定位

`business/domains/point` 负责积分账户和积分变动事实。它不决定某个业务动作是否应该扣积分，也不负责线索展示。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 积分余额 | 按 `userId` 维护用户积分余额 |
| 积分流水 | 记录积分增加、扣减、冻结或调整 |
| 积分事件 | 供充值、解锁等业务触发积分变化 |
| 后台查询 | 平台按用户查看积分余额和流水 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 线索解锁流程 | 属于 user-lead aggregation |
| 支付回调 | 属于 payment / recharge 协作 |
| accountId 归属 | 积分不归属登录账号凭证 |

## 关键技术点

- 积分余额和积分流水归属 `userId`，不归属 `accountId`。
- 积分变更应有可追踪业务来源和幂等约束。
- point domain 提供原子积分能力，跨域业务由 aggregation 编排。

## 职责边界

| 层级 | 职责 |
|---|---|
| point domain | 积分余额、积分流水、积分事件 |
| user-point aggregation | 用户侧积分视图和查询编排 |
| user-lead aggregation | 解锁线索时组合扣积分流程 |
| recharge domain | 充值成功后触发积分增加 |

## 推荐阅读顺序

1. 本文档地图。
2. `backend/src/main/java/com/forest/point`：积分实体、事件和服务。
3. [../../../aggregations/user-point/docs/README.md](../../../aggregations/user-point/docs/README.md)：用户侧积分视图。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| `model.md` | 积分余额、流水和幂等模型 | 待补 |
| `api.md` | 积分查询和后台接口 | 待补 |

## 当前状态

- point domain 已存在后端和前端模块。
- 后续应补充积分幂等、来源单号和异常回滚文档。

## 维护规则

- 积分资产默认归属 `userId`。
- 跨业务扣减和增加流程不要写进 point controller。
