# Forest 多模块 Monorepo 项目

高内聚的多模块 Monorepo 项目。`business/domains/*` 提供单业务域能力，`business/aggregations/*` 提供跨域聚合能力，`apps/*` 负责把多个业务模块组合成完整业务系统。

## 技术栈

**后端：**
- JDK 25 + Spring Boot 4.0.5
- PostgreSQL 18.3
- Spring Data JPA + Maven

**前端：**
- Vue 3.5 + Vite 6
- Turborepo + pnpm workspace
- Changesets 版本管理

## 项目结构

``` 
forest/
├── business/                     # 业务模块（高内聚）
│   ├── domains/                  # 单业务域模块
│   │   ├── user/
│   │   │   ├── backend/          # 用户 + 身份 + 认证 + 后台 admin 认证
│   │   │   └── frontend/         # wechat-miniapp / platform-web 用户能力
│   │   ├── lead/
│   │   │   ├── backend/          # 线索查询 + 后台线索管理
│   │   │   └── frontend/         # wechat-miniapp / platform-web 线索能力
│   │   ├── point/
│   │   │   ├── backend/          # 积分余额 + 流水 + 后台积分查询
│   │   │   └── frontend/         # wechat-miniapp / platform-web 积分能力
│   │   ├── recharge/
│   │   │   ├── backend/          # 充值套餐 + 充值主单
│   │   │   └── frontend/         # wechat-miniapp 充值能力与原生组件
│   │   └── payment/
│   │       ├── backend/          # 支付单 + 微信支付回调
│   │       └── frontend/         # wechat-miniapp 支付能力
│   └── aggregations/             # 跨业务域聚合模块
│
├── apps/                         # 应用系统层
│   ├── trade-leads/              # 外贸线索系统（一个完整 app）
│   │   ├── backend/              # 单一后端运行容器（只放装配 + 编排）
│   │   └── clients/
│   │       ├── platform-web/     # 平台端 PC 前端
│   │       └── client-wechat-miniapp/ # 普通用户微信小程序端
│   ├── attendance/               # 考勤系统（一期 PC Web 骨架）
│   │   ├── backend/              # 后端装配容器
│   │   ├── clients/
│   │   │   ├── admin-web/        # 企业后台 PC 前端
│   │   │   └── platform-web/     # 平台端 PC 前端
│   │   └── docs/                 # app 专属文档
│   └── ai-content-generation/    # AI 内容生成系统（空壳）
│       ├── backend/              # 后端启动壳
│       └── docs/                 # app 专属文档占位
│
├── base-backend/                 # 后端基础设施
│   ├── pom.xml                   # Maven Parent (Spring Boot 4.0.5)
│   └── starter-common/           # 通用依赖 + 自动配置
│
├── base-frontend/                # 前端基础设施
│   ├── packages/ui-kit/          # 基础 UI 组件（FButton, FInput 等）
│   ├── pnpm-workspace.yaml
│   └── tsconfig.base.json
│
├── deploy/                       # Docker Compose 部署资产
│   ├── components/               # 可复用服务组件（postgres/gateway/backend）
│   ├── targets/                  # 按机器角色组合组件
│   ├── scripts/                  # 统一脚本入口
│   └── env/                      # 平台共享环境变量
│
└── docker/                       # 预留给独立脚本、镜像资产等辅助内容
```

## 快速开始

### 1. 准备环境文件

本地需要两类配置：

- 平台共享：`deploy/env/local.env`
- app 专属：`apps/trade-leads/env/local.env`

首次可从 example 复制：

```bash
cp deploy/env/local.env.example deploy/env/local.env
cp apps/trade-leads/env/local.env.example apps/trade-leads/env/local.env
```

### 2. 一条命令启动本地整套

```bash
./deploy/scripts/trade-leads.sh local up
```

这会按机器角色启动：

- `gateway`
- `trade-leads-backend`

启动后访问：

- 前端：`https://localleads.haitunai.cn`
- 后端：`http://127.0.0.1:8081`

首次本地部署前需要确认：

- 本机 hosts 中已将 `localleads.haitunai.cn` 指向 `127.0.0.1`
- `deploy/env/local.env` 已设置 `FOREST_GATEWAY_SSL_DIR` 指向本机证书目录
- `apps/trade-leads/env/local.env` 的数据库连接默认指向 `8.136.34.70:5432/trade_leads_local?sslmode=disable`
- 证书目录按域名分层，例如 `ssl/haitunai.cn/fullchain.pem`、`ssl/haitunai.cn/privkey.pem`
- 宿主机 `443` 端口未被其他服务占用

### 3. 常用本地脚本

完整 local/prod 命令速查见 `apps/trade-leads/README.md` 的“常用命令速查”。

```bash
# 启动本地整套：backend + gateway，数据库连接远程 trade_leads_local
./deploy/scripts/trade-leads.sh local up

# 只启动本地 gateway
./deploy/scripts/gateway.sh local up

# 只启动 backend（连接远程 PostgreSQL）
./deploy/scripts/trade-leads.sh local up backend

# 查看整套日志
./deploy/scripts/trade-leads.sh local logs all

# 仅停止 backend
./deploy/scripts/trade-leads.sh local down

# 停掉本地 gateway
./deploy/scripts/gateway.sh local down
```

### 4. 本地开发模式（可选）

```bash
cd apps/trade-leads/backend
mvn spring-boot:run
```

## 开发工作流

### 后端

```bash
# 本地运行 trade-leads 管理端后端
cd apps/trade-leads/backend
mvn spring-boot:run

# Docker 启动本地整套
./deploy/scripts/trade-leads.sh local up

# Docker 只启动 backend
./deploy/scripts/trade-leads.sh local up backend
```

### 前端

```bash
# 安装依赖
cd base-frontend
pnpm install

# 开发模式
pnpm dev --filter @forest/trade-leads-platform-web

# 使用 Docker + Gateway Nginx 本地部署
./deploy/scripts/trade-leads.sh local up

# 查看日志
./deploy/scripts/trade-leads.sh local logs all

# 停止 backend
./deploy/scripts/trade-leads.sh local down

# 停止本地 gateway
./deploy/scripts/gateway.sh local down

# 构建所有
pnpm turbo build

# 添加变更集
pnpm changeset add
pnpm changeset version
```

### 统一 Gateway Nginx Docker 部署

`gateway` 会先构建 `trade-leads platform-web` 的静态资源，再使用固定稳定版 `nginx:1.28.3-alpine` 作为统一入口。后续其他 `apps/*` 前端也可以继续接入同一个 gateway 镜像与 `conf.d` 站点配置。

当前网关约定：
- 一个 app 一域名
- 一个 app 一份站点片段
- `conf.d/local/*.conf` 与 `conf.d/prod/*.conf` 分别表示不同环境启用的站点集合
- `snippets/sites/*.conf` 只放某个 app 的公共站点逻辑，例如静态根目录和 `/api` 转发

新增 app 接入 gateway 时，显式补齐 4 类内容：
- `apps/gateway/nginx/conf.d/local/<app>.conf`
- `apps/gateway/nginx/conf.d/prod/<app>.conf`
- `apps/gateway/nginx/snippets/sites/<app>.conf`
- `apps/gateway/Dockerfile` 中该 app 的前端 build/copy 段

当前 Deploy 分层：
- `deploy/components/*.yml`：定义可复用服务组件
- `deploy/targets/*.yml`：按机器角色组合组件
- `deploy/scripts/postgres.sh`：管理生产后端机 PostgreSQL，低频操作，默认保留数据卷
- `deploy/scripts/gateway.sh`：管理共享 gateway，负责前端静态资源和 Nginx 入口
- `deploy/scripts/trade-leads.sh`：管理 trade-leads backend，并在本地整套启动时按需委托 gateway 脚本
- `deploy/env/*.env`：平台共享环境变量
- `apps/trade-leads/env/*.env`：trade-leads backend 专属环境变量

容器职责：
- `gateway`：托管前端静态文件，并将 `/api` 请求反向代理到本地 Docker backend 或 prod edge 配置的后端节点集群
- `trade-leads-backend`：提供管理端 API
- `postgres`：生产后端机数据库；本地默认连接远程 `8.136.34.70:5432`，不再启动本地 PostgreSQL Docker
- `prod-core` PostgreSQL 会按 `POSTGRES_PORT` 暴露宿主机端口，方便本地开发直连；服务器安全组应只允许可信开发机 IP 访问该端口

本地 HTTPS：
- `./deploy/scripts/gateway.sh local up` 会映射宿主机 `443` 到 Nginx 容器的 `443`
- 访问地址是 `https://localleads.haitunai.cn`
- 本地 `server_name` 使用 `localleads.haitunai.cn`
- 当前 local target 不映射宿主机 `80`，因此不提供外部 HTTP 入口
- 需要在本机 hosts 中将 `localleads.haitunai.cn` 指向 `127.0.0.1`
- 需要在 `deploy/env/local.env` 里设置 `FOREST_GATEWAY_SSL_DIR`
- 证书目录需要按域名分层，例如 `ssl/haitunai.cn/fullchain.pem`、`ssl/haitunai.cn/privkey.pem`
- 本地构建时会启用 `apps/gateway/nginx/conf.d/local/*.conf`

生产 HTTPS：
- `deploy/env/prod-edge.env` 中将 `FOREST_GATEWAY_SSL_DIR` 指向生产证书目录
- 当前约定生产 edge 服务器使用 `/home/app/trade-leads/ssl`
- 生产 `server_name` 使用 `leads.haitunai.cn`
- 目录结构与本地保持一致，例如 `/home/app/trade-leads/ssl/haitunai.cn/fullchain.pem`
- 生产 edge 通过 `TRADE_LEADS_BACKEND_NODE_1` 与 `TRADE_LEADS_BACKEND_NODE_2` 渲染 `upstream`
- 当前 prod 方案默认最多两个后端节点；如果只用一个节点，可将 `TRADE_LEADS_BACKEND_NODE_2` 留空
- 生产构建时会启用 `apps/gateway/nginx/conf.d/prod/*.conf`

生产代码同步：
- 生产机器默认代码目录是 `/home/code/forest`
- `deploy/env/prod-core.env` 和 `deploy/env/prod-edge.env` 需要配置 `FOREST_CODE_DIR` 与 `FOREST_GIT_REPO_URL`
- `trade-leads` backend 和 gateway 的生产 `up` 会先同步 Git ref，再使用同步后的源码构建镜像
- `trade-leads` backend 和 gateway 的生产 `restart` 同样会先同步 Git ref，再停止旧容器并重建启动
- `postgres` 不依赖业务代码，生产启动不需要 `--ref`
- `apps/trade-leads/env/prod.core.env` 的数据库连接默认指向 ECS 私网 `172.21.11.156:5432/trade_leads_prod?sslmode=disable`

生产启动示例：

```bash
# Prod 后端机：启动 PostgreSQL，不需要 --ref
./deploy/scripts/postgres.sh prod-core up

# Prod 后端机：同步 master 后启动 backend
./deploy/scripts/trade-leads.sh prod-core up --ref master

# Prod 后端机：同步 master 后重启 backend
./deploy/scripts/trade-leads.sh prod-core restart --ref master

# Prod 后端机：同步 v1.2.3 后启动 postgres + backend
./deploy/scripts/trade-leads.sh prod-core up all --ref v1.2.3

# Prod 前端入口机：同步 master 后启动 gateway
./deploy/scripts/gateway.sh prod-edge up --ref master

# Prod 前端入口机：同步 master 后重启 gateway
./deploy/scripts/gateway.sh prod-edge restart --ref master
```

生产敏感文件目录约定：
- 网关 HTTPS 证书：`/home/app/{project-name}/ssl`
- 微信支付密钥：`/home/app/{project-name}/wechat-key`
- `trade-leads` 当前对应：
  - `/home/app/trade-leads/ssl`
  - `/home/app/trade-leads/wechat-key`

常见排错：
- `https://localleads.haitunai.cn` 无法打开：检查 hosts、`443` 端口占用、`FOREST_GATEWAY_SSL_DIR` 是否正确、证书目录是否存在
- 刷新 `/users`、`/point`、`/lead` 出现 404：通常是 gateway 没有正确应用 `try_files ... /index.html`
- 页面能打开但接口报错：先执行 `./deploy/scripts/trade-leads.sh local logs all` 检查 gateway 和 backend 是否完成启动

## 架构理念

### 高内聚
每个业务模块聚焦可复用业务能力，由全栈团队负责演进。

### App 组合
一个 `app` 表示一套完整系统，可以包含多个端；每个 `app` 对应一个独立 `backend` 运行容器，通过组合 `business/domains/*` 与 `business/aggregations/*` 模块形成最终产品。多个 `app` 可以共享同一个 PostgreSQL 容器。

### UI 与逻辑分离
前端内部优先按运行时和功能分组：
- `web/auth/` - 浏览器、PC、H5 通用登录能力
- `web/me/` - 浏览器当前登录用户信息
- `web/user-management/` - 浏览器用户管理能力
- `wechat-miniapp/auth/` - 小程序登录能力
- `wechat-miniapp/me/` - 当前登录用户信息
- `wechat-miniapp/lead-list/` - 小程序线索列表
- `wechat-miniapp/lead-detail/` - 小程序线索详情
- `wechat-miniapp/balance/` - 小程序积分余额
- `wechat-miniapp/logs/` - 小程序积分流水
- `wechat-miniapp/recharge/` - 小程序充值能力与套餐选择组件
- `wechat-miniapp/payment/` - 小程序支付能力
- `platform-web/lead-management/` - 平台线索管理
- `platform-web/point-query/` - 平台积分查询

小程序 app 层只负责页面生命周期、路由、登录态、跨域编排和微信平台 API；业务 UI、展示模型和业务格式化归属对应的 `business/*/frontend`。构建时业务原生小程序组件会装配到 `dist/modules/*`，再由 app 页面通过 `usingComponents` 引用。

### 对象包裹模式
使用组合类型而非展开字段，保证数据原子性和类型安全：

```typescript
interface UserProfile {
  user: User | null
  isLoading: boolean
  error: string | null
}
```

## 包命名规范

### Maven GroupId
- `com.forest.business.*` - 业务模块（如 `user-backend`）
- `com.forest.apps.*` - 应用后端（如 `trade-leads-backend`）
- `com.forest.*` - 基础设施（如 `forest-starter-common`）

### NPM Package
- `@forest/user` - 用户模块前端
- `@forest/lead` - 线索模块前端
- `@forest/point` - 积分模块前端
- `@forest/recharge` - 充值模块前端
- `@forest/payment` - 支付模块前端
- `@forest/trade-leads-platform-web` - Trade Leads 管理端前端
- `@forest/trade-leads-client-wechat-miniapp` - Trade Leads 小程序组合壳
- `@forest/ui-kit` - 基础 UI 组件库

## 文档

- [架构设计文档](docs/architecture/architecture.md)
- [开发规范](docs/guidelines/development-guidelines.md)
- [数据库规范](docs/guidelines/database-guidelines.md)
- [CI/CD 总览](docs/cicd/README.md)
- [CI/CD 分支与触发规则](docs/cicd/branch-and-triggers.md)
- [CI/CD 质量门禁](docs/cicd/quality-gates.md)
- [CI/CD 浏览器质量门禁](docs/cicd/browser-quality-gates.md)
