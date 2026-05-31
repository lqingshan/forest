# 考勤系统运行说明

本文只记录 Attendance app 自身的运行入口、端口、环境变量和验证命令。通用部署规范仍以根目录 `docs/` 下的统一规范为准。

## 后端

后端目录：

```text
apps/attendance/backend
```

默认端口：

```text
8083
```

本地编译：

```bash
mvn -q -pl :forest-app-attendance -am -DskipTests compile
```

本地启动：

```bash
cd apps/attendance/backend
mvn spring-boot:run
```

## 前端

企业后台端：

```bash
cd base-frontend
pnpm --filter @forest/attendance-admin-web dev
pnpm --filter @forest/attendance-admin-web build
```

平台端：

```bash
cd base-frontend
pnpm --filter @forest/attendance-platform-web dev
pnpm --filter @forest/attendance-platform-web build
```

两个 Web 端的 Vite 代理都指向：

```text
http://127.0.0.1:8083
```

## 本地 Docker

Attendance 的本地 Docker 环境只启动应用容器，不启动本地 PostgreSQL / Redis：

```text
attendance-backend
attendance-gateway
```

中间件连接服务器已有 PostgreSQL / Redis。先复制本地 env 模板：

```bash
cp apps/attendance/env/local.docker.env.example apps/attendance/env/local.docker.env
```

然后在 `apps/attendance/env/local.docker.env` 中填写服务器数据库和 Redis 地址。注意数据库必须是 Attendance 专属 dev/test 库，例如 `attendance_dev`，不要连接生产库，也不要复用其他 app 的库。

启动：

```bash
docker compose -f deploy/attendance/docker-compose.local.yml up -d --build
```

日志：

```bash
docker compose -f deploy/attendance/docker-compose.local.yml logs -f attendance-backend
docker compose -f deploy/attendance/docker-compose.local.yml logs -f attendance-gateway
```

停止：

```bash
docker compose -f deploy/attendance/docker-compose.local.yml down
```

默认入口：

```text
http://attendance-admin.localhost
http://attendance-platform.localhost
```

如果本机 80 端口已被占用，可以临时改用 8088：

```bash
ATTENDANCE_GATEWAY_HTTP_PORT=8088 docker compose -f deploy/attendance/docker-compose.local.yml up -d --build
```

访问入口相应改为：

```text
http://attendance-admin.localhost:8088
http://attendance-platform.localhost:8088
```

## 关键配置

| 配置 | 默认值 | 说明 |
|---|---|---|
| `SERVER_PORT` | `8083` | Attendance 后端端口 |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/attendance_local` | 本地开发数据库；Docker 本地部署时改为服务器 Attendance dev/test 库 |
| `FOREST_PLATFORM_ORGANIZATION_NO` | `ORG_PLATFORM` | 平台企业编号 |
| `FOREST_PLATFORM_BOUNDARY_ID` | `0` | 平台治理 RBAC 边界 |
| `REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |

## 当前限制

| 限制 | 说明 |
|---|---|
| Docker 不启动中间件 | 本地 Docker 只启动 backend/gateway，PostgreSQL / Redis 复用服务器服务 |
| Docker env 不提交真实值 | `apps/attendance/env/local.docker.env` 只保存在本地，真实密码不提交 |
| 未建设考勤 domain | 当前只验证 app 装配和 Web 壳 |
