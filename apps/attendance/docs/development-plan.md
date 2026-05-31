# 考勤系统后续开发计划

一期已经建立 app 骨架。后续进入业务建设时，建议按 domain-first 的方式推进。

## 阶段 1：考勤 Domain

新增：

```text
business/domains/attendance
```

建议先拆出以下领域能力：

| 领域 | 说明 |
|---|---|
| 考勤规则 | 工作日、班次、迟到早退、缺卡规则 |
| 考勤组 | 规则适用范围，默认主体为 `organization_member` |
| 打卡记录 | 原始打卡、有效打卡、异常状态 |
| 月度统计 | 面向 HR 的周期统计 |
| 审批关联 | 补卡、外勤、请假、加班等入口 |

## 阶段 2：企业后台接口

企业侧接口建议挂在：

```text
/api/admin/workspace/attendance/**
```

这类接口天然依赖企业工作台上下文：

```text
ADMIN token
+ X-Organization-No
-> organization workspace
-> ORGANIZATION:{organizationId} RBAC
```

建议先实现：

| 接口能力 | 权限点 |
|---|---|
| 考勤规则列表/详情 | `attendance.rule.read` |
| 新增/编辑规则 | `attendance.rule.create`、`attendance.rule.update` |
| 考勤记录查询 | `attendance.record.read` |
| 异常记录处理 | `attendance.record.update` |

## 阶段 3：平台端接口

平台侧接口建议挂在：

```text
/api/platform/attendance/**
```

这类接口不带 `X-Organization-No`，权限上下文来自：

```text
PLATFORM token
-> platform organization guard
-> PLATFORM:{boundaryId} RBAC
```

建议先实现：

| 接口能力 | 权限点 |
|---|---|
| 企业考勤开通状态 | `platform.attendance.organization.read` |
| 企业考勤监管视图 | `platform.attendance.record.read` |
| 平台规则巡检 | `platform.attendance.rule.read` |

## 阶段 4：前端页面

Admin-Web 优先级：

| 页面 | 说明 |
|---|---|
| 考勤规则 | HR 配置规则和适用范围 |
| 考勤记录 | 查看员工打卡明细和异常 |
| 统计报表 | 月度统计、部门统计 |
| 审批入口 | 补卡/外勤/请假等流程入口 |

Platform-Web 优先级：

| 页面 | 说明 |
|---|---|
| 企业监管 | 企业开通、禁用、健康状态 |
| 考勤监管 | 跨企业异常和系统运行视图 |

## 阶段 5：测试策略

| 类型 | 验收点 |
|---|---|
| Domain 测试 | 规则计算、记录状态、统计结果 |
| Admin 接口测试 | `X-Organization-No`、认证 Gate、RBAC |
| Platform 接口测试 | 平台企业准入、`PLATFORM:{boundaryId}` |
| 前端测试 | 路由守卫、按钮权限、企业切换刷新 |
