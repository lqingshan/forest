# Lead 文档地图

Lead 是线索主数据、基础搜索和后台线索管理领域模块。

## 模块定位

`business/domains/lead` 只表达线索本身和基础查询能力，不关心当前用户是否已解锁、不处理积分扣减、不决定小程序展示遮罩。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 线索主数据 | 线索标题、行业、地区、联系方式等基础信息 |
| 线索搜索 | 后台和聚合层可复用的基础搜索能力 |
| 后台管理 | 平台端线索列表、详情、新增、编辑、删除 |
| 解锁基础记录 | 如果存在基础解锁实体，应只表达事实记录 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 当前用户可见性 | 属于 user-lead aggregation |
| 扣积分 | 属于 point domain 或 user-lead 编排 |
| 小程序展示 VO | 属于 user-lead/frontend 或 app 小程序 |

## 关键技术点

- `LeadService.searchPage` 是基础搜索能力，不混入当前用户、积分和遮罩策略。
- 用户侧线索详情和联系方式遮罩由 user-lead aggregation 组合。
- 线索主数据应保持可被多个 app/端复用。

## 职责边界

| 层级 | 职责 |
|---|---|
| lead domain | 线索主数据、基础搜索、后台 CRUD |
| user-lead aggregation | 用户侧可见性、遮罩、解锁编排 |
| point domain | 积分余额和流水 |
| apps/trade-leads | 小程序页面壳和端交互 |

## 推荐阅读顺序

1. 本文档地图。
2. `backend/src/main/java/com/forest/lead`：线索主数据和服务。
3. [../../../aggregations/user-lead/docs/README.md](../../../aggregations/user-lead/docs/README.md)：用户侧线索编排。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| `api.md` | 后台线索接口和查询条件 | 待补 |
| `model.md` | 线索字段和业务状态 | 待补 |

## 当前状态

- 线索 domain 已存在后端和前端模块。
- 用户侧展示和解锁逻辑已从 lead domain 边界外拆出。

## 维护规则

- 线索基础事实写在 lead domain。
- 用户视角、积分、遮罩等组合逻辑写在 user-lead aggregation。
