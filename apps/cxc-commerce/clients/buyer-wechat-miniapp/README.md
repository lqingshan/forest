# Buyer WeChat Miniapp

用户端微信小程序。

## 职责

- 承载 CXC 用户端微信小程序入口。
- 使用微信小程序直接登录能力。
- 使用微信小程序绑定手机号登录能力。
- 调用后端认证接口完成会话创建。
- 承载用户端商品、购物车、下单、支付等小程序流程。

## 当前状态

当前已初始化最小可构建小程序工程：

- 登录页：支持微信直接登录、微信绑定手机号登录。
- 我的页：用于验证登录态和当前用户接口。
- 构建产物目录：`dist`。

常用命令：

```bash
pnpm --dir /Users/lgd/project/forest/base-frontend --filter @forest/cxc-commerce-buyer-wechat-miniapp build:local
pnpm --dir /Users/lgd/project/forest/base-frontend build:cxc-miniapp:local
```

## 边界

- 小程序前端只获取微信 `code` 或微信能力返回的临时凭证。
- 手机号授权由微信小程序 `getPhoneNumber` 获取临时 `phoneCode`，前端不直接解析手机号。
- 微信 `appid / secret`、手机号解析、账号绑定和会话签发必须在后端完成。
- `credential_scope` 由后端根据当前小程序配置确定，不由前端自由传入。
