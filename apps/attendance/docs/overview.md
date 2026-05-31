# 考勤系统概览

Attendance 是独立 app，用于承载企业考勤场景。

一期目标不是实现打卡业务，而是先把 app 入口、端边界、登录准入、企业上下文和文档落稳。这样后续新增考勤 domain 时，可以直接接入既有 user、organization、access 等底座能力。

## 一期范围

| 范围 | 说明 |
|---|---|
| 后端 app | `apps/attendance/backend`，负责 Spring Boot 装配 |
| 企业后台端 | `apps/attendance/clients/admin-web`，面向企业管理员、HR、员工主管 |
| 平台端 | `apps/attendance/clients/platform-web`，面向平台运营/管理员 |
| 企业上下文 | 企业后台端复用 organization workspace，请求头为 `X-Organization-No` |
| 平台准入 | 平台端复用平台企业配置与 `PLATFORM:{boundaryId}` |

## 暂不实现

| 暂不做 | 原因 |
|---|---|
| 移动端/小程序端 | 一期只做 Web PC |
| 考勤业务表 | 先稳定 app 与权限边界 |
| 打卡流程 | 后续进入 `business/domains/attendance` |
| 平台端角色管理 UI | 一期只建设平台登录与占位工作台 |

## 后续权限方向

后续考勤权限点应进入 access 权限目录，例如：

| 权限点 | 含义 |
|---|---|
| `attendance.rule.*` | 考勤规则管理 |
| `attendance.record.*` | 考勤记录查看和修正 |
| `attendance.approval.*` | 考勤审批处理 |
