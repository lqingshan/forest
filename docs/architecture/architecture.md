# Forest 架构说明

相关文档：

- [docs/README.md](../README.md)：docs 目录入口和文档分组
- [docs/documentation-map.md](../documentation-map.md)：全局文档地图，链接到各 app/module 内部文档地图
- [docs/guidelines/development-guidelines.md](../guidelines/development-guidelines.md)：统一开发规范入口，包含 JPA 查询规范
- [docs/guidelines/app-guidelines.md](../guidelines/app-guidelines.md)：app 建壳、文档结构和新 app 前置检查规范
- [business/domains/organization/docs/workspace-guidelines.md](../../business/domains/organization/docs/workspace-guidelines.md)：企业工作台上下文和企业认证 Gate 规范
- [business/domains/access/docs/rbac-guidelines.md](../../business/domains/access/docs/rbac-guidelines.md)：Access、RBAC、权限点和角色规范
- [docs/guidelines/database-guidelines.md](../guidelines/database-guidelines.md)：数据库设计、迁移和 SQL 开发规范
- [business/domains/user/docs/auth-architecture.md](../../business/domains/user/docs/auth-architecture.md)：user/account/auth/session/token 认证边界
- [apps/cxc-commerce/docs/mobile-auth-design.md](../../apps/cxc-commerce/docs/mobile-auth-design.md)：CXC 移动端登录和 JSBridge 认证方案

## 1. 顶层边界

Forest 采用 monorepo + modular monolith。

- `business/domains/*`：单业务域模块，前后端闭环，拥有自己的实体、表、核心服务
- `business/aggregations/*`：跨业务域组合模块，复用 domain 能力，承载多个 app 共享的聚合查询/聚合业务
- `apps/*`：把多个 business 组合成完整业务系统
- `base-backend` / `base-frontend`：最小基础设施层；前端跨 app 能力，例如 HTTP client、微信小程序 platform/session 工厂，应放在这里

外部输入边界必须在进入内部业务层前完成校验、转换和语义拆分。前后端统一遵守 `docs/guidelines/development-guidelines.md` 的“外部边界归一化原则”：边界层可以处理 `unknown`、原始事件、原始请求或第三方响应，内部 service、frontend API、view-model 和 app 编排层只流转明确类型。

当前已有运行 app 包括 `trade-leads` 和 `cxc-commerce`。新增 app 时应继续复用同一套
domain / aggregation / app 分层，不为单个 app 复制通用业务能力。

### 1.1 通用认证架构边界

`user/account/auth/session` 属于通用账号认证能力，统一归属 `business/domains/user`。账号中心只表达自然人、登录凭证、凭证绑定和登录会话，不绑定商城、商家、订单、店铺等具体业务概念。

认证能力的职责边界：

- `business/domains/user`：拥有 `user`、`account`、`user_account`、`auth_session`、`login_log` 等认证核心实体、表和服务。
- `business/domains/verification`：拥有 Redis 验证码、发送冷却、每日上限、错误次数和一次性 verification ticket，不拥有用户账号表。
- `business/domains/notification`：拥有 `sms_send_log` 等通知审计表，记录短信供应商调用结果。
- `base-backend/starter-sms`：封装短信供应商技术能力，一期支持阿里云 SMS、mock 和 disabled。
- `base-backend`：只承载通用认证拦截、认证结果对象、Token 校验抽象等基础设施，不依赖 `business/domains/user` 的业务实现。
- `apps/*`：只做认证能力装配、开放路径配置和 app 专属流程编排，不承载验证码、账号创建、session 创建等通用认证业务逻辑。
- `business/aggregations/*`：不拥有认证实体和表，不直接访问认证 repository。

移动端认证的职责边界：

- H5 负责登录流程编排、短信登录页、手机号绑定页和后端认证接口调用。
- 原生 Android/iOS 本期负责号码认证 SDK、安全存储和 JSBridge 能力；APP 原生微信登录本期不做。
- 后端负责临时凭证换真实身份、账号绑定、会话创建和审计日志。

账号中心遵循“多登录入口，一个身份核心”：手机号是识别自然人的主凭证，本机号、短信验证码、微信小程序手机号授权只是不同登录入口。

通用认证身份链路统一按以下方向理解：

```text
identity/account -> user -> token
```

- `User` 是业务身份核心，业务资产、业务关系和业务流程默认归属 `userId`。
- `Account` / `Identity` 表达登录凭证或第三方身份，解决“这个人如何登录和绑定凭证”的问题。
- `UserAccount` 是 `User` 与登录身份的绑定关系，支持一个用户绑定多个登录方式。
- `accessToken` / `refreshToken` 以 `userId` 作为主要业务身份，`accountId` 只作为登录凭证来源和辅助审计信息。

## 2. 当前目录

```text
forest/
  business/
    domains/
      access/
        backend/
        frontend/
      organization/
        backend/
        frontend/
      user/
        backend/
        frontend/
      lead/
        backend/
        frontend/
      point/
        backend/
        frontend/
      recharge/
        backend/
        frontend/
      payment/
        backend/
        frontend/
      file/
        backend/
        frontend/
      notification/
        backend/
      verification/
        backend/
        frontend/
    aggregations/
      organization-access/
        backend/
        frontend/
      user-lead/
        backend/
        frontend/
      user-point/
        backend/
        frontend/
  apps/
    trade-leads/
      backend/
      clients/
    cxc-commerce/
      backend/
      clients/
        merchant-web/
        platform-web/
    attendance/
      backend/
      clients/
        admin-web/
        platform-web/
    ai-content-generation/
      backend/
    gateway/
      nginx/
  base-backend/
  base-frontend/
  docker/
```

## 3. 模块职责

模块职责以各模块自己的文档地图为准：

| 模块类型 | 文档入口 | 职责 |
|---|---|---|
| app | [docs/documentation-map.md](../documentation-map.md) | 可运行应用、端入口、配置和 app 级编排 |
| domain | [docs/documentation-map.md](../documentation-map.md) | 单业务域实体、表、核心服务和领域规则 |
| aggregation | [business/aggregations/README.md](../../business/aggregations/README.md) | 跨 domain 组合查询、业务编排和可复用视图 |
| base | [docs/documentation-map.md](../documentation-map.md) | 后端 starter、前端 packages 和跨 app 基础设施 |

关键边界：

- `user` 负责 user/account/identity/session/token。
- `organization` 负责企业、部门、员工、认证和 workspace 上下文。
- `access` 负责 RBAC 权限点、角色、授权和权限校验。
- `lead`、`point`、`recharge`、`payment` 分别负责线索、积分、充值和支付事实。
- `user-lead`、`user-point`、`organization-access` 负责跨 domain 编排，不拥有下游 domain 表。

## 4. app 层职责

`apps/trade-leads/backend` 是运行容器，不是第二套业务层。

它只做两件事：

- 装配 `business/domains/user`、`business/domains/lead`、`business/domains/point`，以及需要复用的 `business/aggregations/*`
- 承担跨模块编排

当前属于 app 编排的典型流程：

- 微信登录后自动创建业务用户并初始化积分
- 解锁线索时扣积分

单模块能力不放在 app：

- 用户管理
- 平台端手机号密码 / 手机验证码登录
- 线索 CRUD
- 积分查询

对微信小程序 app，还要进一步区分两类职责：

- `base-frontend/packages/wechat-miniapp-platform` 负责微信运行时的底层封装，例如 `wx.request`、`wx.login`、`wx.requestPayment`、storage、router、session store
- `apps/*/clients/client-wechat-miniapp` 或 `apps/*/clients/buyer-wechat-miniapp` 负责这些平台能力的调用时机、页面流程和跨 domain 编排

也就是说：

- 微信 API 的“怎么封装”在 `base-frontend`
- 微信 API 的“什么时候调用、调用后跳到哪里”在 app Page

当前明确留在微信小程序 app 层的固定资产包括：

- 页面生命周期：`onLoad`、`onShow`、`onPullDownRefresh`、`onReachBottom`
- 登录守卫：`ensureClientSession(...)`
- app 路由策略：`openPrimaryPage(...)`、`replacePage(...)`、`goBackOr(...)`
- 微信平台动作：`wx.showModal`、`wx.showToast`、`wx.showLoading`、`wx.hideLoading`、`wx.setClipboardData`
- 微信小程序支付调起时机：`requestWechatMiniappPayment(...)`
- app 级壳组件和导航，例如 `bottom-nav`
- 页面壳布局，例如 `shell-page`、`shell-stack`、`shell-back`

这些能力不下沉到 business frontend，因为它们回答的是“这个小程序客户端如何运行”，而不是“单一业务该如何展示”。

## 5. 前端结构规则

每个 business 的 frontend 只依赖本模块自己的 API，不依赖 app API，也不依赖别的 business API。
domain frontend 可以沉淀本域 API、类型、展示模型和不跨域的展示组件；完整页面如果需要组合多个 domain 或 app 平台能力，仍放在 `apps/*/clients/*`。
原生小程序组件可以放在 domain frontend，由 app 小程序构建脚本复制并打包到 `dist/modules/*` 后通过 `usingComponents` 引用。

`base-frontend` 承载跨 app 的前端基础设施：

- `@forest/http-client`：统一 HTTP client 和可注入 transport
- `@forest/wechat-miniapp-platform`：微信小程序 `wx.request`、storage、router、payment、login code 等平台适配
- `@forest/wechat-miniapp-client-session`：通用小程序 client session 编排工厂，通过注入业务 API 使用，不直接依赖 business 包
- `@forest/wechat-miniapp-client-app`：把微信小程序 platform、client session 和 app definition 组装成 app facade

当前统一按真实端命名：

```text
business/domains/user/frontend/src/
  wechat-miniapp/
    auth/
    me/
  platform-web/
    auth/
    me/
    user-management/

business/domains/lead/frontend/src/
  wechat-miniapp/
    lead-list/
    lead-detail/
  platform-web/
    lead-management/

business/domains/point/frontend/src/
  wechat-miniapp/
    balance/
    logs/
  platform-web/
    point-query/

business/domains/recharge/frontend/src/
  wechat-miniapp/
    recharge/      # 套餐、充值单 API、本域套餐展示模型、原生小程序套餐选择组件

business/domains/payment/frontend/src/
  wechat-miniapp/
    payment/       # 支付单 API 和支付参数类型

business/domains/file/frontend/src/
  shared/           # 文件 API、类型、大小和类型校验
  web/
    upload/         # PC/H5 上传编排
  wechat-miniapp/
    upload/         # 小程序上传编排和原生组件
```

`apps/trade-leads/clients/*` 只做组合壳：

- `platform-web`：可运行的后台前端壳
- `wechat-miniapp`：小程序组合壳，负责原生 Page、app 专属路由配置、登录 redirect 策略、业务模块组合和跨域流程；微信平台基础设施通过 `base-frontend` 公共包装配，业务 WXML/WXSS 归属 business frontend

微信小程序页面的成熟度，不按“代码行数越少越好”判断，而按“职责是否放对层”判断：

- `login / leads / unlocked / point-logs` 这类页面，如果已经只保留登录守卫、生命周期、刷新/触底、路由和平台动作，可以视为比较稳定
- `me / lead-detail / recharge / payment-result` 这类页面，即使代码量仍偏大，只要复杂度来自跨 domain 编排或微信平台动作，也属于合理的 app 组合页
- 判断页面是否还应继续下沉时，不看它“厚不厚”，而看它是否还在持有本应属于 business 的文案、状态映射、分页 merge、业务卡片结构

通用 domain frontend 的视觉样式由 app 接管。domain 组件只提供结构、状态和样式 hook，不写死 app 品牌色、卡片阴影和页面布局；文件上传组件这类跨 app 复用组件必须遵守这一点。

## 6. 当前认证、企业和权限边界

Forest 当前把登录身份、企业工作台准入和 RBAC 权限拆成三层：

- `user` domain 负责 user/account/identity/session/token。
- `organization` domain 负责企业、员工、认证状态和企业工作台上下文。
- `access` domain 负责 RBAC 权限点、角色、角色权限和授权关系。

企业后台的典型请求链路：

```text
ADMIN token
+ X-Organization-No
-> OrganizationWorkspaceInterceptor
-> OrganizationWorkspaceAspect
-> PermissionAspect
-> controller/service
```

平台后台的典型请求链路：

```text
PLATFORM token
-> PlatformLoginAccessGuard
-> PlatformAccessContextInterceptor
-> PermissionAspect
-> controller/service
```

## 7. 后台范围

后台能力按 app 和 module 拆分，不再用根架构文档维护单一“后台功能清单”。

- Trade Leads 后台能力以用户、线索、积分、充值、支付为主。
- CXC Commerce 后台能力拆成商家端和平台端，商家端复用 organization workspace 和 access RBAC。
- Attendance 一期只建设 platform-web 和 admin-web 骨架，后续考勤业务进入独立 domain。
- 角色权限管理属于 `access` domain + `organization-access` aggregation。

## 8. 依赖原则

- `business` 不能依赖 `apps`
- `business/domains/*` 不能依赖 `business/aggregations/*`
- `business/aggregations/*` 可以依赖 `business/domains/*`
- `app` 可以组合多个 `business`
- `business/*/frontend` 只能依赖本模块 API
- `base-frontend` 的微信小程序 platform/session 包不能依赖 `business/*`
- `business/*/frontend` 不能依赖 `apps/*`，也不能依赖微信小程序 app 装配包
- `controller -> service interface -> impl -> repository`
- app 只做编排，不重写单模块接口
