# Access / RBAC 规范

本文档沉淀 Access 模块、RBAC 权限点、角色、授权、平台边界和权限上下文规则。

相关文档：

- 总开发规范：[development-guidelines.md](../../../../docs/guidelines/development-guidelines.md)
- 企业工作台规范：[workspace-guidelines.md](../../organization/docs/workspace-guidelines.md)
- 架构边界：[architecture.md](../../../../docs/architecture/architecture.md)

## 1. 分层原则

权限相关判断分为四层，不互相替代：

| 层级 | 判断什么 | 不判断什么 |
|---|---|---|
| `accessScope` | token 是否能进入 client/admin/platform API 前缀 | 不判断企业、角色、权限点 |
| 企业工作台准入 | user 是否是当前企业 ACTIVE member，企业状态是否可进入 | 不判断具体业务权限 |
| 企业认证 Gate | 企业认证状态是否允许访问该功能 | 不通过删权限实现认证限制 |
| RBAC | 当前 member 是否拥有某个业务权限点 | 不判断企业认证状态 |

## 2. 权限主体

一期权限主体统一是：

```text
organization_member
```

不是 `user`。

| 场景 | 身份 |
|---|---|
| 商家 A 员工 | `organization_member(商家A)` |
| 商家 B 员工 | `organization_member(商家B)` |
| 平台企业员工 | `organization_member(平台企业)` |

平台治理权限也挂在平台企业的 `organization_member` 上，只是 RBAC 边界是 `PLATFORM:{boundaryId}`。

## 3. 权限上下文

`AccessCheckContext` 表达一次权限判断所需的主体和边界。

| 字段 | 含义 |
|---|---|
| `subjectType` | 权限主体类型，一期为 `ORGANIZATION_MEMBER` |
| `subjectId` | 主体 ID，即 `organization_member.id` |
| `boundaryType` | 权限生效边界，例如 `ORGANIZATION` / `PLATFORM` |
| `boundaryId` | 具体边界 ID，例如企业 ID / 平台治理边界 ID |

企业后台工作台：

```text
ORGANIZATION_MEMBER:{memberId}
ORGANIZATION:{organizationId}
```

平台治理：

```text
ORGANIZATION_MEMBER:{platformMemberId}
PLATFORM:{forest.platform.boundary-id}
```

约束：

- RBAC 上下文必须由请求层在进入业务前准备好。
- `PermissionAspect` 只读取既有 `AccessCheckContext`，不再猜测上下文。
- 业务代码创建上下文时优先使用工厂方法，例如 `AccessCheckContext.organizationMember(...)`。

## 4. 权限点

权限点按业务能力设计，不按 URL 或 Controller 设计。

示例：

| 权限点 | 含义 |
|---|---|
| `organization.read` | 查看企业信息 |
| `organization.update` | 编辑企业信息 |
| `organization.department.*` | 部门管理 |
| `organization.member.*` | 员工管理 |
| `access.role.*` | 角色管理 |
| `access.assignment.read` | 查看员工角色分配 |
| `access.assignment.manage` | 修改员工角色分配 |
| `platform.organization.certification.review` | 平台审核企业认证 |

权限点源头：

```text
List<PermissionDefinition>
```

启动时构建内存视图：

| 视图 | 用途 |
|---|---|
| `codeSet` | 校验权限点唯一 |
| `codeMap` | 根据 code 获取定义 |
| `permissionTree` | 给前端角色授权页展示 |
| `wildcardIndex` | 展开 `organization.member.*` |
| `annotationIndex` | 校验注解引用的权限点 |

权限点定义不入 DB。

## 5. 权限注解

接口权限判断通过注解表达，不通过 URL 映射猜权限。

| 注解 | 说明 |
|---|---|
| `@RequirePermission` | 当前接口需要某一个权限点 |
| `@RequireAllPermissions` | 当前接口需要全部权限点 |
| `@RequireAnyPermission` | 当前接口拥有任意一个权限点即可 |

规则：

- 标了权限注解才做 RBAC 判断。
- 没有权限注解默认不做 RBAC 判断。
- 权限注解支持 Controller 级和方法级。
- 接口注解必须使用精确权限点，不使用通配符。
- 后台关键接口可以做“缺少权限注解提醒”，但不作为一期阻塞规则。

## 6. 通配符

通配符只用于角色授权，不用于接口注解和前端 `can()`。

| 场景 | 是否支持通配符 |
|---|---|
| 角色权限授权 | 支持，例如 `organization.member.*` |
| 接口权限注解 | 不支持，必须精确权限点 |
| 前端 `can()` | 不支持，使用后端展开后的精确权限集合 |

管理接口拒绝全局 `*`。

## 7. 角色模型

权限点全局共用，角色按边界隔离。

| 对象 | 范围 |
|---|---|
| 权限点 | 全局共用 |
| 企业角色 | `ORGANIZATION:{organizationId}` |
| 平台治理角色 | `PLATFORM:{boundaryId}` |

关键表：

| 表 | 作用 |
|---|---|
| `access_role` | 角色 |
| `access_role_permission` | 角色拥有的权限点或通配符 |
| `access_role_assignment` | 员工被分配了哪些角色 |

系统预设角色使用：

```text
systemPreset = true
```

含义：

- 系统初始化角色。
- 不允许改名、禁用、删除或改权限。
- 自定义角色 `systemPreset = false`，允许编辑、停用和删除。

## 8. 默认企业角色

| 角色 code | 名称 | 默认权限模式 |
|---|---|---|
| `organization_owner` | 企业所有者 | `organization.*`、`access.*` |
| `organization_admin` | 企业管理员 | `organization.read`、`organization.update`、`organization.certification.submit`、`organization.department.*`、`organization.member.*` |
| `organization_member` | 普通员工 | `organization.read`、`organization.department.read`、`organization.member.read` |

默认企业角色在企业创建后初始化。

## 9. Platform-Web 准入

平台端使用配置化平台企业和平台治理边界：

```yaml
forest:
  platform:
    organization-no: ${FOREST_PLATFORM_ORGANIZATION_NO:ORG_PLATFORM}
    boundary-id: ${FOREST_PLATFORM_BOUNDARY_ID:0}
```

规则：

- platform-web 登录准入 = 当前 user 是配置的平台企业 ACTIVE 员工。
- platform 接口权限判断 = 当前平台企业 member 在 `PLATFORM:{boundaryId}` 下查角色。
- 平台企业自己的组织管理仍使用 `ORGANIZATION:{platformOrganizationId}`。
- 平台治理角色和平台企业内部角色不要混成一个边界。

## 10. 员工停用

停用 member 不删除角色和权限关系。

效果：

- `DISABLED member` 不出现在企业切换列表。
- `DISABLED member` 不能进入企业工作台。
- 重新启用后原角色关系继续生效。
