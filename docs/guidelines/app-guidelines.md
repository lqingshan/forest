# App 开发规范

本文档沉淀 `apps/*` 的职责、建壳流程、文档结构和新 app 前置检查。

相关文档：

- 总开发规范：[development-guidelines.md](./development-guidelines.md)
- 架构边界：[architecture.md](../architecture/architecture.md)
- Web 前端规范：[frontend-web-guidelines.md](./frontend-web-guidelines.md)

## 1. app 职责

`apps/*` 是应用系统层，只负责把多个 business 模块组合成完整业务系统。

允许放：

- 后端运行装配。
- app 专属配置。
- app client 路由、布局和页面壳。
- app 专属流程编排。
- app 专属文档、需求、部署说明。

不应放：

- 通用单业务域能力。
- 多个 app 都会复用的聚合查询。
- 可复用的组织、权限、用户、支付等核心逻辑。

## 2. 新 app 前置检查

新建 app 前先确认：

| 问题 | 处理 |
|---|---|
| 是新业务 app 还是新业务 domain | app 只做装配；可复用业务能力先放 domain 或 aggregation |
| 是否复用 user / organization / access | 登录、企业、员工、权限优先复用已有底座 |
| 是否需要企业工作台 | 需要则沿用 `X-Organization-No` + workspace context，不写入 auth token |
| 是否需要平台端 | 需要则复用 platform 企业准入和 `PLATFORM:{boundaryId}` |
| 是否需要独立前端端口和 app config | 每个 app client 独立 `package.json`、`app.config.ts`、`storagePrefix` |
| 是否需要后端 app reactor | 新 app backend 需要同步考虑 Maven module 和应用专用 reactor |

## 3. 后端 app

后端 app 默认目录：

```text
apps/<app>/backend
```

后端 app 负责：

- Spring Boot 启动类。
- app `application.yml`。
- app WebMvcConfig。
- 当前 app 需要装配的 starter、domain、aggregation 依赖。
- 当前 app 的 Flyway migration classpath 复制。

后端 app 不应直接实现可复用业务逻辑。

## 4. 前端 clients

前端 client 默认目录：

```text
apps/<app>/clients/<client>
```

每个 client 必须有独立：

- `package.json`
- `app.config.ts`
- `session.ts`
- `storagePrefix`
- 路由入口

端命名建议：

| 端 | 目录 |
|---|---|
| 企业后台 PC | `admin-web` |
| 平台后台 PC | `platform-web` |
| 买家微信小程序 | `buyer-wechat-miniapp` |
| 客户端微信小程序 | `client-wechat-miniapp` |
| 移动 H5 | `mobile-h5` |

## 5. app 文档结构

每个 app 推荐文档结构：

```text
apps/<app>/
├── README.md
└── docs/
    ├── overview.md
    ├── architecture.md
    ├── development-plan.md
    ├── runtime.md
    ├── clients.md
    ├── requirements/
    │   ├── README.md
    │   ├── source/
    │   ├── extracted/
    │   └── accepted/
    ├── features/
    └── decisions/
```

目录边界：

| 目录 | 放什么 |
|---|---|
| `requirements/source` | 原始 Word、PPT、PDF、Excel |
| `requirements/extracted` | 从原始材料提炼出的 Markdown 摘要 |
| `requirements/accepted` | 已确认的需求基线 |
| `features` | 具体业务功能方案，偏“怎么实现” |
| `decisions` | 关键取舍，记录“为什么这样选” |

通用开发规范仍放根目录 `docs/guidelines`，不要复制到 app 文档里。

## 6. 验证

新 app 最小验证：

```bash
mvn -q -pl :forest-app-<app> -am -DskipTests compile
pnpm --filter @forest/<app-client> build
git diff --check
```

只建后端空壳时，可以只执行后端编译和 `git diff --check`。
