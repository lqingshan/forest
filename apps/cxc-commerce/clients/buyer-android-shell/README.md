# Buyer Android Shell

用户端 Android 原生壳。

## 职责

- 提供 Android 原生 WebView 容器。
- 接入号码认证 SDK，实现本机手机号一键登录 token 获取。
- 接入微信 Android SDK，实现微信授权 code 获取。
- 提供 `ForestNative.auth` JSBridge 能力。
- 使用 Android 安全存储保存 refreshToken 等敏感会话信息。

## 边界

- 原生壳不承载商城业务规则。
- 原生壳不直接创建后端会话，只把原生 SDK 返回的临时凭证交给 H5 / 后端流程。
- 微信 appSecret、号码认证服务端密钥不得下发到客户端。
