# 考勤系统端说明

Attendance 一期只建设两个 PC Web 端。

| 端 | 路径 | accessScope | 使用对象 |
|---|---|---|---|
| 企业后台端 | `clients/admin-web` | `ADMIN` | 企业管理员、HR、员工主管 |
| 平台端 | `clients/platform-web` | `PLATFORM` | 平台运营、平台管理员 |

## Admin-Web

包名：

```text
@forest/attendance-admin-web
```

本地 storage prefix：

```text
forest.attendance.admin
```

企业后台端使用普通 `ADMIN` token，企业工作台接口通过请求头声明当前企业：

```text
X-Organization-No: ORG_xxx
```

后端每次请求都会用当前 `userId + organizationNo` 校验 ACTIVE `organization_member`，不信任前端本地状态。

当前路由：

| 路由 | 说明 |
|---|---|
| `/login` | 手机号密码/验证码登录 |
| `/organizations` | 我的企业、创建企业、选择企业 |
| `/certification` | 企业资料和认证入口 |
| `/access` | 企业角色权限管理 |
| `/attendance` | 考勤占位工作台 |

## Platform-Web

包名：

```text
@forest/attendance-platform-web
```

本地 storage prefix：

```text
forest.attendance.platform
```

平台端使用 `PLATFORM` token，不使用 `X-Organization-No`。

平台准入依赖：

```yaml
forest:
  platform:
    organization-no: ORG_PLATFORM
    boundary-id: 0
```

当前路由：

| 路由 | 说明 |
|---|---|
| `/login` | 平台手机号密码/验证码登录 |
| `/dashboard` | 平台首页占位 |
| `/organizations` | 企业监管占位 |
| `/attendance` | 考勤监管占位 |

## 后续端规划

| 端 | 规划 |
|---|---|
| 移动端 | 后续员工打卡、外勤、补卡等场景再评估 |
| 小程序端 | 后续看企业员工打卡入口是否需要 |
| 平台角色管理 UI | 一期不做，后续单独评估 |
