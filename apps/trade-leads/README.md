# Trade Leads App

## Deploy Layout

当前部署入口按“组件 + 机器角色 + 脚本”组织：

- `deploy/components/*.yml`: 可复用服务组件
- `deploy/targets/*.yml`: 机器角色组合
- `deploy/scripts/postgres.sh`: 生产后端机 PostgreSQL 入口
- `deploy/scripts/gateway.sh`: 共享 gateway 入口
- `deploy/scripts/trade-leads.sh`: trade-leads backend 入口
- `deploy/env/*.env`: 平台共享环境变量
- `apps/trade-leads/env/*.env`: trade-leads backend 专属环境变量

## 常用命令速查

推荐启动命令：

```bash
# Local：启动本地整套，只包含 backend + gateway；数据库连接远程 trade_leads_local。
./deploy/scripts/trade-leads.sh local up

# Prod 后端机：启动 PostgreSQL，不需要 --ref。
./deploy/scripts/postgres.sh prod-core up

# Prod 后端机：同步 master 后启动 backend。
./deploy/scripts/trade-leads.sh prod-core up --ref master

# Prod 后端机：同步 master 后重启 backend。
./deploy/scripts/trade-leads.sh prod-core restart --ref master

# Prod 前端入口机：同步 master 后启动 gateway。
./deploy/scripts/gateway.sh prod-edge up --ref master

# Prod 前端入口机：同步 master 后重启 gateway。
./deploy/scripts/gateway.sh prod-edge restart --ref master
```

本地环境：

```bash
# 启动整套：backend + gateway
./deploy/scripts/trade-leads.sh local up

# 只启动 gateway
./deploy/scripts/gateway.sh local up

# 只启动 backend
./deploy/scripts/trade-leads.sh local up backend

# 重启本地 backend
./deploy/scripts/trade-leads.sh local restart backend

# 查看整套状态
./deploy/scripts/trade-leads.sh local ps all

# 查看整套日志
./deploy/scripts/trade-leads.sh local logs all

# 停 backend
./deploy/scripts/trade-leads.sh local down

# 停 gateway
./deploy/scripts/gateway.sh local down
```

生产后端机 `prod-core`：

```bash
# 启动 postgres，不需要 --ref
./deploy/scripts/postgres.sh prod-core up

# 同步 master 后启动 backend
./deploy/scripts/trade-leads.sh prod-core up --ref master

# 同步 master 后重启 backend
./deploy/scripts/trade-leads.sh prod-core restart --ref master

# 同步指定 tag 后启动 postgres + backend
./deploy/scripts/trade-leads.sh prod-core up all --ref v1.2.3

# 查看 postgres 状态
./deploy/scripts/postgres.sh prod-core ps

# 查看 backend 状态
./deploy/scripts/trade-leads.sh prod-core ps

# 查看 backend 日志
./deploy/scripts/trade-leads.sh prod-core logs
```

生产前端入口机 `prod-edge`：

```bash
# 同步 master 后启动 gateway
./deploy/scripts/gateway.sh prod-edge up --ref master

# 同步 master 后重启 gateway
./deploy/scripts/gateway.sh prod-edge restart --ref master

# 查看 gateway 状态
./deploy/scripts/gateway.sh prod-edge ps

# 查看 gateway 日志
./deploy/scripts/gateway.sh prod-edge logs

# 停 gateway
./deploy/scripts/gateway.sh prod-edge down
```

生产环境里，`backend/gateway` 的 `up/restart` 必须带 `--ref`；`postgres` 不需要。

## Local

准备本地配置：

```bash
cp deploy/env/local.env.example deploy/env/local.env
cp apps/trade-leads/env/local.env.example apps/trade-leads/env/local.env
mkdir -p apps/trade-leads/wechat/merchant/cert
```

本地后端默认不再启动 PostgreSQL Docker，直接连接远程数据库：

```text
jdbc:postgresql://8.136.34.70:5432/trade_leads_local?sslmode=disable
```

首次切换到 app 独立数据库时，先创建新库并从旧库复制数据：

```bash
./deploy/scripts/postgres-db.sh local create trade-leads
./deploy/scripts/postgres-db.sh local copy-legacy trade-leads
./deploy/scripts/postgres-db.sh local verify trade-leads
```

因此本地 `trade-leads.sh local up` 只负责启动 `backend + gateway`。远程 `prod-core` 上的 PostgreSQL 需要通过 `POSTGRES_PORT` 暴露宿主机端口，并在 ECS 安全组中只放行可信开发机 IP。

把微信支付证书放到：

```text
apps/trade-leads/wechat/merchant/cert/apiclient_key.pem
apps/trade-leads/wechat/merchant/cert/wechatpay_public_key.pem
```

启动本地整套：

```bash
./deploy/scripts/trade-leads.sh local up
```

只启动本地 gateway：

```bash
./deploy/scripts/gateway.sh local up
```

只启动 backend：

```bash
./deploy/scripts/trade-leads.sh local up backend
```

查看整套日志：

```bash
./deploy/scripts/trade-leads.sh local logs all
```

停止 backend：

```bash
./deploy/scripts/trade-leads.sh local down
```

停止本地 gateway：

```bash
./deploy/scripts/gateway.sh local down
```

## Production

当前生产拓扑拆成两台机器：

- `prod-core`: `postgres + trade-leads-backend`
- `prod-edge`: `gateway`

准备 shared env：

```bash
cp deploy/env/prod-core.env.example deploy/env/prod-core.env
cp deploy/env/prod-edge.env.example deploy/env/prod-edge.env
```

生产 `up` 会先把 `/home/code/forest` 同步到指定 Git ref，再使用同步后的源码构建镜像。
因此 `trade-leads` backend 和 gateway 的生产启动命令必须带 `--ref`；PostgreSQL 不依赖业务代码，不需要 `--ref`。

生产后端默认连接远程生产库：

```text
jdbc:postgresql://172.21.11.156:5432/trade_leads_prod?sslmode=disable
```

生产切换到 app 独立数据库时，需要在维护窗口内创建新库并复制旧库数据：

```bash
./deploy/scripts/postgres-db.sh prod-core create trade-leads
./deploy/scripts/postgres-db.sh prod-core copy-legacy trade-leads
./deploy/scripts/postgres-db.sh prod-core verify trade-leads
```

旧库 `forest_prod` 会保留，回滚时可把 `SPRING_DATASOURCE_URL` 切回旧库。

`deploy/env/prod-core.env` 和 `deploy/env/prod-edge.env` 需要配置：

```text
FOREST_CODE_DIR=/home/code/forest
FOREST_GIT_REPO_URL=<your-git-repository-url>
```

准备 trade-leads backend env：

```bash
cp apps/trade-leads/env/prod.core.env.example apps/trade-leads/env/prod.core.env
mkdir -p /home/app/trade-leads/wechat-key
```

把生产微信支付证书放到：

```text
/home/app/trade-leads/wechat-key/apiclient_key.pem
/home/app/trade-leads/wechat-key/wechatpay_public_key.pem
```

在后端机启动 postgres：

```bash
./deploy/scripts/postgres.sh prod-core up
```

在后端机只启动 backend：

```bash
./deploy/scripts/trade-leads.sh prod-core up --ref master
```

在后端机同步代码并重启 backend：

```bash
./deploy/scripts/trade-leads.sh prod-core restart --ref master
```

在后端机一起启动 `postgres + backend`：

```bash
./deploy/scripts/trade-leads.sh prod-core up all --ref master
```

在前端入口机启动 gateway：

```bash
./deploy/scripts/gateway.sh prod-edge up --ref master
```

在前端入口机同步代码并重启 gateway：

```bash
./deploy/scripts/gateway.sh prod-edge restart --ref master
```

生产 edge 必须在 `deploy/env/prod-edge.env` 里设置：

```text
TRADE_LEADS_BACKEND_NODE_1=<backend-node-1>:8081
TRADE_LEADS_BACKEND_NODE_2=<backend-node-2>:8081
```

当前 prod gateway 会在构建时把这两个节点渲染进 `upstream trade_leads_backend_upstream`。
如果生产暂时只有一个 backend 节点，可以把 `TRADE_LEADS_BACKEND_NODE_2` 留空。

当前生产敏感文件路径约定：

```text
/home/app/trade-leads/ssl
/home/app/trade-leads/wechat-key
```

## Notes

应用读取的微信支付证书路径固定为：

```text
/run/secrets/wechat-pay/apiclient_key.pem
/run/secrets/wechat-pay/wechatpay_public_key.pem
```

不要提交任何真实密钥文件，也不要提交 `wechat/merchant/cert/` 目录内容。
