# Business Aggregations

This directory contains business modules that aggregate multiple domain modules.

Aggregation modules may depend on `business/domains/*` modules, but domain modules must not depend on aggregation modules.

## 模块定位

`business/aggregations/*` 用于承载跨 domain 的业务编排、组合查询和前端视图。它不拥有下游 domain 的核心实体和表，也不反向污染 domain 边界。

## 当前模块

| 模块 | 文档地图 | 定位 |
|---|---|---|
| `organization-access` | [organization-access/docs/README.md](./organization-access/docs/README.md) | 企业工作台内的角色权限管理编排 |
| `user-lead` | [user-lead/docs/README.md](./user-lead/docs/README.md) | 用户侧线索展示、可见性和解锁编排 |
| `user-point` | [user-point/docs/README.md](./user-point/docs/README.md) | 用户积分查询和用户侧积分视图 |

## 关键规则

- aggregation 可以依赖 domain。
- domain 不能依赖 aggregation。
- aggregation 不拥有 domain 的表和核心实体。
- 多个 app 可复用的跨域流程优先沉淀到 aggregation。
- 单 app 独有且不具备复用价值的页面壳和端流程留在 app。

## 维护规则

新增 aggregation 时必须同步补充：

1. `business/aggregations/<aggregation>/docs/README.md`
2. 根 [docs/documentation-map.md](../../docs/documentation-map.md)
3. 本文件的当前模块清单
