# Base Frontend 文档地图

Base Frontend 是前端公共基础设施层，提供跨 app 复用的 packages。

## 模块定位

`base-frontend` 承载 HTTP client、UI kit、微信小程序平台适配和客户端 session 工厂等公共能力，不承载具体 app 页面和业务规则。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| api-contracts | 跨前端工程共享的 API 类型和契约 |
| http-client | 统一 HTTP client、transport 和拦截能力 |
| ui-kit | 通用 UI 组件和样式基础 |
| wechat-miniapp-platform | 微信小程序平台 API 封装 |
| wechat-miniapp-client-session | 小程序 client session 编排工厂 |
| wechat-miniapp-client-app | 小程序 app facade 组装 |
| wechat-miniapp-ui | 小程序通用 UI 能力 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| app 页面 | 页面壳和路由属于 apps/*/clients |
| domain 业务组件 | 单业务域组件属于 business/domains/*/frontend |
| 跨域业务页面 | 属于 business/aggregations/*/frontend 或 app |

## 关键技术点

- base package 应通过注入配置或 adapter 使用业务 API，避免直接依赖 app/domain。
- 微信平台能力的“怎么封装”在 base-frontend，“什么时候调用”在 app 页面。
- `http-client` 是跨 app 请求基础设施，具体 token、header 和错误处理策略由 app 接入时配置。
- UI kit 不应写死单个 app 的品牌色和业务文案。
- 业务组件优先沉淀在 domain/aggregation frontend；base-frontend 只承载无业务语义的通用组件。

## 职责边界

| 层级 | 职责 |
|---|---|
| base-frontend | 公共前端基础设施和跨 app packages |
| business frontend | 单业务域 API、类型、展示模型和可复用组件 |
| aggregation frontend | 跨 domain 的页面和组件编排 |
| apps/*/clients | 可运行端应用、路由、页面壳和 app 状态 |

## 推荐阅读顺序

1. 本文档地图。
2. [../packages/http-client](../packages/http-client)：统一 HTTP client。
3. [../packages/ui-kit](../packages/ui-kit)：通用 UI 基础。
4. [../packages/wechat-miniapp-platform](../packages/wechat-miniapp-platform)：微信小程序平台适配。
5. [../../docs/guidelines/frontend-web-guidelines.md](../../docs/guidelines/frontend-web-guidelines.md)：Web 前端规范。
6. [frontend-components.md](./frontend-components.md)：Base Frontend 组件地图。
7. [../../docs/guidelines/frontend-component-reuse.md](../../docs/guidelines/frontend-component-reuse.md)：前端组件复用规范。

## 文档清单

| Package | 内容 | 状态 |
|---|---|---|
| `api-contracts` | API 类型和契约 | 已有代码，文档待补 |
| `http-client` | HTTP client 和 transport | 已有代码，文档待补 |
| `ui-kit` | 通用 UI 组件 | 已有代码，文档待补 |
| [frontend-components.md](./frontend-components.md) | Base Frontend 组件地图 | 已有 |
| `wechat-miniapp-platform` | 小程序平台能力 | 已有代码，文档待补 |
| `wechat-miniapp-client-session` | 小程序 session 工厂 | 已有代码，文档待补 |
| `wechat-miniapp-client-app` | 小程序 app facade | 已有代码，文档待补 |
| `wechat-miniapp-ui` | 小程序 UI 基础 | 已有代码，文档待补 |

## 当前状态

- base-frontend 已提供多个跨 app packages。
- 包级文档仍需继续补齐，尤其是 http-client、微信小程序 platform/session 和 UI kit。

## 维护规则

- 公共平台封装写在 base-frontend。
- 业务语义、页面流程和 app 状态不要下沉到 base package。
- 新增通用组件前，先确认它不属于某个业务 domain 或 aggregation。
