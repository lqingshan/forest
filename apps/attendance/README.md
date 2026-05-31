# Attendance App

考勤系统独立 app，后端 artifact 为 `forest-app-attendance`，Java 包名为 `com.forest.attendance`。

一期只建设 PC Web 骨架：

| 端 | 目录 | 说明 |
|---|---|---|
| 平台端 | `clients/platform-web` | 平台运营/管理员使用，登录使用 `PLATFORM` accessScope |
| 企业后台端 | `clients/admin-web` | 企业管理员、HR、员工主管使用，登录使用 `ADMIN` accessScope |

## Backend

启动入口：

```text
apps/attendance/backend
```

本 app 后端只做装配，不放通用考勤业务逻辑。后续考勤规则、记录、审批等能力应沉淀到：

```text
business/domains/attendance
```

编译：

```bash
mvn -q -pl :forest-app-attendance -am -DskipTests compile
```

## Frontend

企业后台：

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

两个前端默认代理到 attendance 后端：

```text
http://127.0.0.1:8083
```

## Docs

- [系统概览](docs/overview.md)
- [架构说明](docs/architecture.md)
- [开发计划](docs/development-plan.md)
- [运行部署](docs/runtime.md)
- [端说明](docs/clients.md)
- [需求文档](docs/requirements/README.md)
- [功能方案](docs/features/README.md)
- [决策记录](docs/decisions/README.md)
