# User 认证架构

本文档沉淀 user domain 的认证边界：用户、账号、登录身份、会话和 token。

相关文档：

- [User 文档地图](./README.md)
- [Forest 架构说明](../../../../docs/architecture/architecture.md)
- [CXC 移动端登录方案](../../../../apps/cxc-commerce/docs/mobile-auth-design.md)
- [号码认证技术抽象](../../../../base-backend/docs/carrier-auth.md)

## 1. 认证边界

user domain 只处理账号、用户、认证和会话，不混入商城、组织、店铺、订单等业务概念。

统一身份链路：

```text
identity/account -> user -> token
```

| 对象 | 作用 |
|---|---|
| `User` | 业务身份核心，业务资产默认归属 `userId` |
| `Account` / `Identity` | 登录凭证或第三方身份 |
| `UserAccount` | `User` 与登录身份的绑定关系 |
| `AuthSession` | 登录会话、refresh token 和登录审计 |
| token | 以 `userId` 为主要业务身份，`accountId` 只做辅助审计 |

## 2. 账号模型

账号唯一性：

```text
type + credential_scope + identifier
```

本期常见凭证：

| 登录凭证 | type | credential_scope | identifier |
|---|---|---|---|
| 手机号 | `phone` | `GLOBAL` | 标准化手机号 |
| 用户手机号密码 | `phone_password` | `GLOBAL` | 标准化手机号 |
| 历史平台登录名 | `platform_password` | `GLOBAL` | 登录名 |
| 微信小程序 | `wechat_miniapp` | `appCode` | openid |

APP 本机号一键登录不创建单独运营商账号，认证成功后最终落到 `phone/GLOBAL`。

本期不新增：

```text
wechat_app
```

## 3. 登录方式

### APP 本机号一键登录

```text
carrierToken
-> CarrierAuthClient.resolvePhone
-> 获得可信手机号
-> 标准化手机号
-> 创建或复用 phone/GLOBAL account
-> 创建或复用 user
-> 创建 auth_session
-> 返回 accessToken / refreshToken
```

### H5 短信验证码登录

```text
手机号
-> 发送短信验证码
-> 校验验证码
-> 创建或复用 phone/GLOBAL account
-> 创建或复用 user
-> 创建 auth_session
```

### H5 手机号密码登录

```text
手机号 + 密码
-> 校验 phone_password/GLOBAL account
-> 找到 user
-> 创建 auth_session
```

## 4. 后端接口语义

本机手机号一键登录接口：

```text
POST /api/auth/carrier/one-click-login
```

请求字段：

| 字段 | 说明 |
|---|---|
| `carrierToken` | 原生号码认证 SDK 返回的一次性取号凭证 |
| `provider` | 原生侧供应商标识，例如 `ALIYUN` |
| `appCode` | 客户端应用编码 |
| `clientType` | `ANDROID_APP` 或 `IOS_APP` |
| `accessScope` | `CLIENT / ADMIN / PLATFORM` |

user domain 只表达“通过可信手机号创建或复用 user/account/session”。号码认证供应商 SDK 细节归 base-backend。

## 5. 约束

- 后端是唯一可信认证边界，H5 和原生只拿临时凭证。
- 外部输入进入 user domain 前必须完成校验、转换和语义命名。
- `accessToken` / `refreshToken` 不写入企业工作台上下文。
- 企业员工身份归 organization，权限归 access。
- 业务资产不要归属到 `accountId`。

## 6. 本期不做

- APP 原生微信登录。
- `wechat_app` account。
- 微信 APP 绑定手机号。
- 企业、店铺、部门、权限 ThreadLocal。
