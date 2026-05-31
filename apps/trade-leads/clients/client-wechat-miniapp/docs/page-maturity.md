# Trade Leads Miniapp Page Maturity

这份文档记录 `trade-leads` 微信小程序当前 8 个页面的成熟度、app 层固定资产边界，以及本轮架构收口后已经跑通的验证命令。

## 1. 边界复述

当前小程序端的职责分层固定为：

- `base-frontend/packages/wechat-miniapp-platform`：
  微信小程序运行时适配。负责 `wx.request`、`wx.login`、`wx.requestPayment`、storage、router、session store 的底层封装。
- `base-frontend/packages/wechat-miniapp-client-session`：
  通用 client session 编排。负责登录、恢复、刷新、401 清理，但只接受注入函数，不直接依赖 business。
- `base-frontend/packages/wechat-miniapp-client-app`：
  通用 app 装配工厂。把 platform、client session 和 app definition 组装成具体小程序可直接使用的 facade。
- `apps/trade-leads/clients/client-wechat-miniapp`：
  trade-leads 自己的 app 装配层。负责页面、一级导航、生命周期、微信 API 的调用时机、跨 domain 编排。
- `business/*/frontend/src/wechat-miniapp`：
  业务 API、业务 view-model、业务 state/pager、业务展示组件。

一句话记忆：

- 微信 API 的底层封装在 `base-frontend`
- 微信 API 的调用时机和页面流程编排在 `app`
- 业务展示和业务状态模型在 `business`

## 2. App 固定资产清单

下面这些能力现在明确留在 app 层，不继续下沉到 business：

- 页面生命周期：
  `onLoad`、`onShow`、`onPullDownRefresh`、`onReachBottom`
- 登录守卫：
  `miniappAuth.ensureClientSession(...)`
- app 路由策略：
  `miniappRouter.openPrimaryPage(...)`、`replacePage(...)`、`goBackOr(...)`
- 微信平台动作：
  `wx.showModal`、`wx.showToast`、`wx.showLoading`、`wx.hideLoading`、`wx.setClipboardData`
- 微信小程序支付调起时机：
  `miniappPayment.requestWechatMiniappPayment(...)`
- app 级壳组件和导航：
  `src/components/bottom-nav`
- 页面壳布局：
  `shell-page`、`shell-stack`、`shell-back`

这些资产留在 app 层的原因都一样：
它们不是单一业务规则，而是“这个微信小程序客户端如何运行和如何导航”的问题。

## 3. 页面成熟度表

| 页面 | 当前角色 | 成熟度 | 目前判断 | 下一步 |
| --- | --- | --- | --- | --- |
| `pages/login` | app 登录编排页 | 高 | 页面只保留 redirect 解析、loading/toast、登录成功后跳转；登录文案和面板已在 `user/frontend/wechat-miniapp/auth` | 保持现状 |
| `pages/leads` | 一级主页面 + user-lead 装配页 | 高 | 分页状态机已下沉到 `user-lead/list/pager.ts`，业务展示已下沉到 `lead-list-panel`，页面保留登录守卫、刷新、触底、打开详情 | 保持现状 |
| `pages/unlocked` | 一级主页面 + user-lead 装配页 | 高 | 已解锁列表状态机已下沉到 `user-lead/unlocked/state.ts`，业务展示已下沉到 `unlocked-lead-panel` | 保持现状 |
| `pages/me` | 一级主页面 + 跨 `user + point` 组合页 | 中高 | 这是天然组合页，仍需要并行装配当前用户和积分余额，还要承载退出登录和跨域导航 | 不追求机械下沉，维持组合页定位 |
| `pages/lead-detail` | user-lead 详情页 + app 平台动作页 | 中高 | 详情状态机已下沉到 `detail-state.ts`，业务展示已下沉到 `lead-detail-panel`；页面仍必须保留复制、modal、去充值、resumeUnlock | 保持现状，继续避免把微信动作下沉到 business |
| `pages/recharge` | recharge 业务页 + 微信小程序支付编排页 | 中高 | 套餐页状态已下沉到 `recharge/state.ts`，业务展示已下沉到 `recharge-panel`；页面仍必须保留创建支付单、拉起支付、跳结果页 | 保持现状，等待支付命名兼容期结束后再看是否要收尾 |
| `pages/point-logs` | point 查询页 | 高 | 分页状态机已下沉到 `point/logs/pager.ts`，展示已下沉到 `point-log-panel`，页面只保留守卫、刷新、触底、返回 | 保持现状 |
| `pages/payment-result` | app 结果页 | 中高 | 结果展示状态已下沉到 `recharge/result-state.ts`，但轮询和结果页跳转天然属于 app 层 | 保持现状 |

整体结论：

- `login / leads / unlocked / point-logs` 已经进入“比较稳定”的状态
- `me / lead-detail / recharge / payment-result` 仍然会比其它页面厚一些，但这是由跨 domain 编排和微信平台动作决定的，不是简单的架构退化

## 4. 验证命令清单

下面这些命令在 2026-04-23 已经实际执行通过。

### 4.1 小程序端

```bash
cd /Users/lgd/project/forest/apps/trade-leads/clients/client-wechat-miniapp
pnpm --filter @forest/trade-leads-client-wechat-miniapp build
```

### 4.2 小程序基础设施公共包

```bash
cd /Users/lgd/project/forest/base-frontend
pnpm --filter @forest/wechat-miniapp-platform test
pnpm --filter @forest/wechat-miniapp-client-session test
pnpm --filter @forest/wechat-miniapp-client-app test
```

### 4.3 后端 payment 模块

注意：
后端不能直接在 `business/domains/payment/backend` 目录单跑 `mvn test`，否则容易因为缺少上游 snapshot 依赖而失败。

正确做法是从 `base-backend/pom.xml` 这个 reactor 入口启动，并带 `-am` 自动构建依赖模块。

```bash
cd /Users/lgd/project/forest
mvn -f /Users/lgd/project/forest/base-backend/pom.xml \
  -pl ../business/domains/payment/backend \
  -am test
```

### 4.4 后端 user-point aggregation 模块

```bash
cd /Users/lgd/project/forest
mvn -f /Users/lgd/project/forest/base-backend/pom.xml \
  -pl ../business/aggregations/user-point/backend \
  -am test
```

## 5. 仍待继续收口的事项

这轮主架构已经收住，后续优先级按下面顺序推进：

1. 持续使用 reactor 入口补更多后端模块测试，避免再用错误的单模块 Maven 命令
2. 继续监控是否有人重新引入 `src/platform/*`、`src/app-platform.ts`、`src/auth/client-session.ts`
3. 等支付渠道历史值全部迁移稳定后，再评估是否移除 `WECHAT_JSAPI` 的兼容读取
4. 未来如果新增第二个微信小程序客户端，直接复用 `@forest/wechat-miniapp-platform`、`@forest/wechat-miniapp-client-session`、`@forest/wechat-miniapp-client-app`，不要复制 app/platform 底层实现
