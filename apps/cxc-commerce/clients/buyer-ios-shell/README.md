# Buyer iOS Shell

用户端 iOS 原生壳。

## 职责

- 提供 iOS 原生 WebView 容器。
- 接入号码认证 SDK，实现本机手机号一键登录 token 获取。
- 提供 `ForestNative.auth` JSBridge 能力。
- 使用 Keychain 保存 refreshToken 等敏感会话信息。

## 首版约束

- 首版 iOS 暂不接微信登录。
- iOS 用户端登录方式优先支持本机手机号一键登录和其他手机号短信登录。

## 边界

- 原生壳不承载商城业务规则。
- 原生壳不直接解析手机号，手机号解析必须由后端调用号码认证服务端接口完成。
- 号码认证服务端密钥不得下发到客户端。
