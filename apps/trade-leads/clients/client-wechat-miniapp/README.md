# Trade Leads WeChat Miniapp

这个目录是 `trade-leads` 微信小程序 app 装配层。

它负责：

- 小程序 `app.json`、页面、导航和 sitemap
- app 专属路由策略、登录 redirect、storage prefix、后端地址装配
- 组合 `business/domains/*/frontend` 与 `business/aggregations/*/frontend` 的小程序业务组件
- 跨 domain 流程，例如积分不足跳充值、充值支付后跳结果页

它不负责：

- 通用微信平台基础设施。`wx.request`、storage、router、payment、login code 和 session store 在 `@forest/wechat-miniapp-platform`
- 通用 client session 编排。登录、恢复、刷新、401 清理由 `@forest/wechat-miniapp-client-session` 提供工厂，当前 app 只注入 `@forest/user` API
- 通用小程序 app 装配工厂。`@forest/wechat-miniapp-client-app` 负责把 platform + client session + app definition 组装成统一 facade
- 业务 UI、业务文案、业务展示模型。它们应放在对应 `business/*/frontend/src/wechat-miniapp`

当前 app 的装配入口是：

- `src/app-definition.ts`：trade-leads 特有配置和业务注入
- `src/miniapp-app.ts`：装配后的统一 app facade
- `src/app.ts`：只处理小程序生命周期

不要重新新增 `src/platform/*`、`src/app-platform.ts` 或 `src/auth/client-session.ts` 这类通用实现；如果多个小程序都会复用，优先上移到 `base-frontend/packages/*`。

学习文档：

- [docs/login-page.md](/Users/lgd/project/forest/apps/trade-leads/clients/client-wechat-miniapp/docs/login-page.md)：`src/pages/login/index.ts` 的逐行讲解与页面层职责说明
- [docs/page-maturity.md](/Users/lgd/project/forest/apps/trade-leads/clients/client-wechat-miniapp/docs/page-maturity.md)：8 个页面的成熟度表、app 固定资产边界和已实跑的验证命令
- [docs/payment-integration-summary.md](/Users/lgd/project/forest/apps/trade-leads/clients/client-wechat-miniapp/docs/payment-integration-summary.md)：微信小程序支付对接总结、关键配置、验收 SQL 和排障经验

常用命令：

```bash
pnpm --filter @forest/trade-leads-client-wechat-miniapp typecheck
pnpm --filter @forest/trade-leads-client-wechat-miniapp check:architecture
pnpm --filter @forest/trade-leads-client-wechat-miniapp build
```

前端打包命令：

```bash
pnpm --dir /Users/lgd/project/forest/base-frontend build:trade-leads-miniapp:local
pnpm --dir /Users/lgd/project/forest/base-frontend build:trade-leads-miniapp:prod
```

其中：

- `build:trade-leads-miniapp:local` 打 local 包，后端地址为 `https://localleads.haitunai.cn`
- `build:trade-leads-miniapp:prod` 打 prod 包，后端地址为 `https://leads.haitunai.cn`
- 两个命令都会注入 `MINIAPP_APP_CODE=trade-leads-miniapp`
