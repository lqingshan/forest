# CXC Commerce APP 介绍

## 1. 定位

CXC Commerce 是面向 CXC 客户的独立 B2B 多商家商城应用实例。

该项目服务定制化客户场景，设计目标是让每个客户 APP 拥有独立的用户中心、账号中心、认证会话和业务数据，同时复用 Forest 平台内可沉淀的通用基础能力和业务域能力。

## 2. 命名

项目代码目录：

```text
apps/cxc-commerce
```

项目名称：

```text
CXC Commerce
```

中文业务名称：

```text
CXC B2B 多商家商城
```

选择 `commerce` 而不是 `shop`，是因为该项目不是单一店铺系统，而是覆盖商品、类目、商家、店铺、购物车、订单、支付、履约、结算、对账、运营活动等完整电商交易体系。`shop` 保留给业务模型里的“店铺”概念。

## 3. 应用结构

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

## 4. 端应用范围

| 端类型 | 应用目录 | 说明 |
| --- | --- | --- |
| 用户端 Android | `clients/buyer-android-shell` + `clients/buyer-mobile-h5` | Android 原生壳提供 SDK、JSBridge、安全存储，H5 承载用户端移动业务页面 |
| 用户端 iOS | `clients/buyer-ios-shell` + `clients/buyer-mobile-h5` | iOS 原生壳提供号码认证、JSBridge、Keychain，H5 承载用户端移动业务页面 |
| 用户端微信小程序 | `clients/buyer-wechat-miniapp` | 承载微信小程序用户端入口、微信直接登录、微信绑定手机号登录和小程序交易流程 |
| 商家端 PC | `clients/merchant-web` | 承载商家后台工作台 |
| 平台端 PC | `clients/platform-web` | 承载平台运营和管理后台 |
| 后端应用 | `backend` | CXC 项目级装配、API 暴露、配置入口和模块组合 |

## 5. 客户隔离模型

CXC Commerce 是独立客户 APP 实例，不与其他客户 APP 共享用户中心、账号中心、会话和业务数据。

```text
CXC Commerce
  -> 独立 user
  -> 独立 account
  -> 独立 auth_session
  -> 独立 business data
```

不同客户即使使用相同手机号，也应识别为不同客户实例下的独立用户。

## 6. 通用认证关系

CXC 项目本身不重新发明账号中心。通用认证能力仍归属：

```text
business/domains/user
```

该通用域负责：

- `user`
- `account`
- `user_account`
- `auth_session`
- `login_log`
- Redis 验证码能力
- 短信发送日志
- 手机号登录
- 微信登录凭证识别
- 微信小程序绑定手机号登录
- token 签发和校验

`apps/cxc-commerce` 只负责项目级装配：

- `appCode`
- `clientCode`
- 开放路径
- 按 `appCode` 管理微信小程序 `appid/secret`
- 移动端号码认证配置
- 端应用路由和页面入口

## 7. 账号凭证范围

同一个 CXC APP 内部可能存在多个微信小程序、移动端 APP 或其他第三方登录入口，因此账号凭证需要保留 `credential_scope`。

```text
type + credential_scope + identifier
```

示例：

| type | credential_scope | identifier |
| --- | --- | --- |
| `phone` | `GLOBAL` | `13800138000` |
| `phone_password` | `GLOBAL` | `13800138000` |
| `platform_password` | `GLOBAL` | `+8618257147892`，历史平台登录名/迁移数据 |
| `wechat_miniapp` | `cxc-commerce-buyer-wechat-miniapp` | `openid_xxx` |

`credential_scope` 只表达“这个凭证在哪个命名空间内唯一”，不表达是否允许登录某个端。端登录准入后续由独立的登录权限 / RBAC / IAM 能力承载。

## 8. 移动端登录策略

用户端移动登录采用：

```text
登录页用 H5
原生壳提供 SDK 能力
后端统一创建 auth_session
```

### Android

支持：

- 本机手机号一键登录
- 其他手机号短信登录

Android 原生壳负责获取运营商认证 token，后端负责解析手机号、绑定账号和创建会话。本期不做 APP 原生微信登录。

### iOS

首版支持：

- 本机手机号一键登录
- 其他手机号短信登录

首版暂不接微信登录，降低 iOS 上架和第三方登录合规复杂度。

### 微信小程序

支持微信小程序直接登录和微信小程序绑定手机号登录。

微信小程序前端只获取微信临时凭证：

- `wx.login()` 返回的登录 `code`。
- `getPhoneNumber` 返回的手机号授权 `code`。

后端使用当前小程序对应配置调用微信接口，完成账号识别、手机号解析、账号绑定和会话创建。

对 CXC Commerce 来说，微信小程序绑定手机号登录不是预留能力，而是当前基础认证阶段需要纳入的必备能力。

## 9. 当前初始化范围

本次只初始化 CXC 项目结构和项目介绍文档。

包含：

- `apps/cxc-commerce`
- `backend`
- `clients/buyer-mobile-h5`
- `clients/buyer-wechat-miniapp`
- `clients/buyer-android-shell`
- `clients/buyer-ios-shell`
- `clients/merchant-web`
- `clients/platform-web`
- `env`

暂不包含：

- 可运行 Spring Boot 后端应用
- 可运行前端工程
- Android / iOS 原生工程代码
- 商城业务域实现
- 组织、部门、RBAC、商家、店铺、订单等业务模型实现

## 10. 后续建议

下一步建议继续落地通用身份认证基础模块：

1. `account` 增加 `credential_scope`。
2. 唯一键改为 `type + credential_scope + identifier`。
3. 短信验证码内容按配置生效，并走完整 Redis 校验链路。
4. 完成微信小程序直接登录和绑定手机号登录的账号模型适配。
5. 保持 CXC 应用只做装配，不把通用认证逻辑写进 `apps/cxc-commerce`。
