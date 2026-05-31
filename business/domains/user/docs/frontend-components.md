# User 前端组件地图

本文档登记 user domain frontend 已沉淀的可复用能力。app 开发登录、当前用户展示、用户管理相关 UI 前，必须先查本地图。

## Web PC 登录

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `WebPcLoginFlow` | `@forest/user/web/auth` | PC Web 登录页，支持手机号密码和手机验证码登录 | app 登录页优先使用 |
| `PhonePasswordLoginPanel` | `@forest/user/web/auth` | 只需要手机号密码登录的局部面板 | 仅在不需要完整登录流时使用 |
| `PhoneSmsLoginPanel` | `@forest/user/web/auth` | 只需要手机验证码登录的局部面板 | 仅在不需要完整登录流时使用 |
| `createWebAuthSession` 等 session 工厂 | `@forest/user/web/auth` | app 构建自己的 token storage、restore、logout 状态 | 不在 app 重复写 token/localStorage/session 编排 |

## 当前用户展示

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `CurrentUserBadge` | `@forest/user/web/me` | layout、侧边栏、顶部栏展示当前用户头像、姓名、手机号 | 禁止在 app layout 中手写 `currentUser.name/phone/avatarUrl` 展示 |

## 用户管理

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `UserManagementWorkspace` | `@forest/user/web/user-management` | 平台或后台用户管理完整工作台 | 优先作为用户管理页主体 |
| `UserTable` | `@forest/user/web/user-management` | 用户列表表格 | 列表展示不要重复写 |
| `UserFilterBar` | `@forest/user/web/user-management` | 用户筛选条 | 筛选交互不要重复写 |
| `UserDetailPanel` | `@forest/user/web/user-management` | 用户详情侧栏或面板 | 详情展示不要重复写 |
| `UserStatusActions` | `@forest/user/web/user-management` | 用户状态操作 | 状态按钮逻辑不要重复写 |
| `UserCard` | `@forest/user/web/user-management` | 用户摘要卡片 | 摘要展示优先复用 |

## Review 检查

- app 登录页是否使用 `WebPcLoginFlow`。
- layout 是否使用 `CurrentUserBadge` 展示当前用户。
- 是否把 user 管理 CRUD 写在 app 页面里。
- 如果现有组件不满足，优先改造 user frontend 组件，而不是在 app 复制一份。
