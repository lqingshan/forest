# User Point 文档地图

User Point 是用户积分查询和用户侧积分视图聚合模块。

## 模块定位

`business/aggregations/user-point` 组合 user 与 point 能力，向 app 提供用户视角的积分余额、积分流水和积分展示数据。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 用户积分余额 | 查询当前 user 的积分余额 |
| 用户积分流水 | 查询当前 user 的积分变动记录 |
| 平台查询视图 | 为平台/后台提供按用户维度的积分视图 |
| 前端视图 | 提供 platform-web 或小程序可复用的积分查询 UI |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 积分账户底层模型 | 属于 point domain |
| 积分扣减业务流程 | 由具体 aggregation 编排 |
| 充值支付流程 | 属于 recharge/payment |

## 关键技术点

- 积分资产以 `userId` 为主体。
- point domain 提供积分事实，本 aggregation 提供用户视角查询和展示组合。
- 如果查询同时需要 user 基础信息和 point 数据，应放在本 aggregation，而不是让 point 依赖 user。

## 职责边界

| 层级 | 职责 |
|---|---|
| user-point aggregation | 用户维度积分视图和查询编排 |
| point domain | 积分余额、流水和事件 |
| user domain | 用户基础信息 |
| apps | 页面入口和端交互 |

## 推荐阅读顺序

1. 本文档地图。
2. [../../../domains/point/docs/README.md](../../../domains/point/docs/README.md)：point domain 文档地图。
3. [../../../domains/user/docs/README.md](../../../domains/user/docs/README.md)：user domain 文档地图。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| `api.md` | 用户积分查询接口说明 | 待补 |
| `frontend.md` | 积分查询页面和组件说明 | 待补 |

## 当前状态

- 后端和 platform-web frontend 模块已存在。
- 仍需补充查询接口、分页和展示规则文档。

## 维护规则

- 用户维度组合查询放在本模块。
- 积分底层变更和流水规则写在 point domain。
