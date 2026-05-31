# User 文档地图

User 是 Forest 的通用用户、账号、登录身份、session 和 token 领域模块。

## 模块定位

`business/domains/user` 负责自然人、账号凭证、登录身份绑定、认证会话和用户管理。它不表达具体 app 的企业、积分、线索、订单等业务资产。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 用户主体 | `User` 是业务身份核心 |
| 账号凭证 | 手机号、密码、第三方 identity 等登录方式 |
| 绑定关系 | `UserAccount` 表达 user 与登录身份的绑定 |
| 登录认证 | 手机号密码、验证码、微信小程序等登录流程 |
| session/token | accessToken、refreshToken、auth_session 和登录审计 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 企业员工身份 | 属于 organization 的 `organization_member` |
| RBAC 权限 | 属于 access domain |
| 积分/线索资产 | 属于 point、lead 和对应 aggregation |

## 关键技术点

- 认证链路统一理解为 `identity/account -> user -> token`。
- `User` 是业务身份核心；业务资产默认归属 `userId`。
- `Account` / `Identity` 表达登录凭证或第三方身份。
- `UserAccount` 是 `User` 与登录身份的绑定关系，支持一个用户绑定多个登录方式。
- token 以 `userId` 为主要业务身份，`accountId` 只作为凭证来源和辅助审计信息。
- Web PC 登录页优先复用 `WebPcLoginFlow`，当前用户展示优先复用 `CurrentUserBadge`。

## 职责边界

| 层级 | 职责 |
|---|---|
| user domain | 用户、账号、身份绑定、session、token |
| verification domain | 验证码、冷却、校验票据 |
| notification domain | 短信发送日志和通知审计 |
| organization domain | 企业员工身份和企业准入 |
| apps | 登录页面、端准入和 app 装配 |

## 推荐阅读顺序

1. 本文档地图。
2. [auth-architecture.md](./auth-architecture.md)：user/account/auth/session/token 认证架构。
3. [frontend-components.md](./frontend-components.md)：user 前端组件地图。
4. [../../../../docs/architecture/architecture.md](../../../../docs/architecture/architecture.md)：通用认证架构边界。
5. `backend/src/main/java/com/forest/user/auth`：认证入口。
6. `backend/src/main/java/com/forest/user/useraccount`：用户与账号绑定关系。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| [auth-architecture.md](./auth-architecture.md) | 用户、账号、identity、session、token 设计 | 已有 |
| [frontend-components.md](./frontend-components.md) | user 前端组件、流程和复用规则 | 已有 |
| `api.md` | 登录、刷新 token、用户管理接口 | 待补 |

## 当前状态

- user/account/auth/session 已沉淀为通用 domain。
- 多端登录能力和验证码能力通过 verification、notification 等模块协作。
- 与企业权限相关的身份切换由 organization/access 处理。

## 维护规则

- 登录身份和认证会话写在 user domain。
- 验证码细节写在 verification domain。
- 业务资产归属不要写到 accountId。
- app 不要手写 user 已提供的登录流、当前用户展示和用户管理组件。
