# Buyer Mobile H5

用户端移动 H5，嵌入 Android / iOS 原生壳。

## 职责

- 承载用户端移动业务页面。
- 承载移动端登录页和短信手机号登录交互。
- 通过 JSBridge 调用原生能力，如本机手机号一键登录、微信授权、token 安全存储。
- 使用后端 `auth/session` 能力完成可信登录和会话创建。

## 边界

- 不直接接入运营商 SDK。
- 不直接接入微信原生 SDK。
- 不在 H5 localStorage 中保存 refreshToken 或长期 accessToken。
