# CXC 移动端登录方案

本文档沉淀 CXC Commerce Android/iOS 原生壳 + H5 的移动端登录方案。

相关文档：

- [CXC Commerce 文档地图](./README.md)
- [User 认证架构](../../../business/domains/user/docs/auth-architecture.md)
- [号码认证技术抽象](../../../base-backend/docs/carrier-auth.md)
- [移动端登录和 JSBridge 全局边界](../../../docs/architecture/architecture.md)

## 1. 目标

CXC 移动端登录采用混合架构：

```text
首屏体验：号码认证 SDK 授权页
流程编排：H5
原生能力：Android/iOS 号码认证 SDK、JSBridge、安全存储
认证边界：后端 user/account/auth/session
```

## 2. 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| APP 原生本机号一键登录 | Android/iOS 原生壳拉起号码认证 SDK |
| H5 手机号短信验证码登录 | 作为本机号能力不可用时的降级方案 |
| H5 手机号密码登录 | 作为 Web/H5 通用能力 |
| 原生安全存储 | refreshToken 推荐由原生安全存储承载 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| APP 原生微信登录 | 微信 APP 登录无法直接获得手机号 |
| `wechat_app` account | 本期不新增该账号类型 |
| 微信 APP 绑定手机号 | 会增加绑定链路复杂度，暂不做 |
| 企业/店铺权限判断 | 移动端登录只解决 user 登录身份 |

## 3. 关键技术点

- 后端是唯一可信认证边界，H5 和原生只拿临时凭证。
- 真实手机号必须由后端通过 `CarrierAuthClient` 调供应商解析。
- 最终身份统一归集到 `phone + GLOBAL + normalizedPhone`。
- Web/H5、微信小程序、APP 原生壳分别封装运行时能力。
- app 层注入 `appCode / clientType / accessScope / storagePrefix`。

## 4. 登录流程

### APP 本机号一键登录

```text
用户打开 App
-> 原生检测号码认证能力
-> 拉起号码认证 SDK 授权页
-> 用户确认本机号码一键登录
-> 原生获得 carrierToken
-> H5 调用 /api/auth/carrier/one-click-login
-> 后端通过 CarrierAuthClient 解析可信手机号
-> 创建或复用 phone/GLOBAL account
-> 创建或复用 user
-> 创建 auth_session
-> 返回 accessToken / refreshToken
```

### H5 短信验证码登录

```text
用户点击其他手机号登录
-> H5 展示短信验证码登录页
-> 用户输入手机号
-> 发送短信验证码
-> 用户输入验证码
-> 后端校验验证码
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

## 5. JSBridge 协议

H5 不直接调用原生 SDK，只调用统一门面：

```text
ForestNative.auth
```

本期能力：

| 能力 | 说明 |
|---|---|
| `requestCarrierLoginToken` | 拉起号码认证 SDK，返回 `carrierToken` |

推荐返回：

```json
{
  "carrierToken": "token-from-native-sdk",
  "provider": "ALIYUN"
}
```

常见错误：

```text
USER_CANCEL
CARRIER_UNAVAILABLE
SDK_NOT_INITIALIZED
NETWORK_ERROR
TOKEN_EXPIRED
AUTH_FAILED
UNKNOWN_ERROR
```

## 6. Token 存储

推荐 APP 场景：

| Token | 存储位置 |
|---|---|
| refreshToken | 原生安全存储 |
| iOS refreshToken | Keychain |
| Android refreshToken | Keystore / EncryptedSharedPreferences |
| accessToken | H5 内存短期使用 |

当前 Web/H5 通用实现仍支持 `storagePrefix` 隔离本地 token。后续 APP 原生壳接入安全存储时，可以用 JSBridge 替换 H5 存储实现。

## 7. 降级与异常

| 场景 | 处理 |
|---|---|
| 本机号能力不可用 | 进入短信验证码登录 |
| 用户取消授权 | 留在登录页或进入短信验证码登录 |
| carrierToken 为空 | 拒绝登录 |
| carrierToken 换手机号失败 | 提示失败，允许短信登录 |
| 供应商 disabled | 明确提示本机号一键登录不可用 |

## 8. 职责边界

| 层级 | 职责 |
|---|---|
| CXC Android/iOS shell | 号码认证 SDK、JSBridge、安全存储 |
| CXC H5 | 登录流程编排、页面交互、降级入口 |
| user domain | account/user/session/token |
| starter-carrier-auth | 供应商 SDK 抽象和手机号解析 |
| verification/notification | 验证码和短信发送审计 |
