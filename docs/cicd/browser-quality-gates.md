# Forest 浏览器质量门禁

Purpose: 定义 Playwright 浏览器自动化测试在发布流程中的分层、用例矩阵与数据策略。
Scope: 当前覆盖 `apps/trade-leads/clients/platform-web` 管理端浏览器自动化测试。
Owner: 管理端前端负责人 / 测试负责人联合维护。
Last Updated: 2026-04-15
Related Docs:

- [quality-gates.md](./quality-gates.md)
- [branch-and-triggers.md](./branch-and-triggers.md)

## 1. 原则

- 同一条浏览器测试可以属于多个层级，但职责必须清楚。
- `PR Smoke` 要快且稳。
- `Release Regression` 优先只读或低污染场景。
- `Write Regression` 承担高风险写路径验证，但不默认阻塞正式发版。
- `Post-Deploy Smoke` 只能使用只读检查。

## 2. 标签规范

Playwright 用例应使用统一标签前缀：

- `@smoke`
- `@regression`
- `@write`
- `@postdeploy`
- `@readonly`

示例：

```ts
test('@smoke @regression AUTH-001 登录成功', async ({ page }) => {
  // ...
})
```

## 3. 分层定义

### 3.1 PR Smoke

目标：判断改动能不能合并。

要求：

- 运行时间短
- 失败定位清晰
- 不依赖复杂测试数据编排

### 3.2 Release Regression

目标：判断候选版本是否可发布。

要求：

- 运行在 staging
- 以查询、跳转、详情、分页、校验为主
- 避免环境污染型写操作

### 3.3 Write Regression

目标：验证写操作流程在真实页面上仍然可用。

要求：

- 独立测试数据
- 可重复执行
- 默认不作为正式发版阻塞项

### 3.4 Post-Deploy Smoke

目标：验证生产刚发布后系统是否可用。

要求：

- 只读
- 极小集合
- 快速返回结果

## 4. 当前已实现用例矩阵

| 用例 | 当前标签 | 说明 |
| --- | --- | --- |
| `AUTH-001` 登录成功 | `@smoke @regression @postdeploy @readonly` | 基础认证烟雾与发布后确认 |
| `AUTH-003` 退出登录与路由守卫 | `@smoke @regression` | 认证链路与受保护路由 |
| `POINT-001` 旧积分路由跳转 | `@smoke @regression @postdeploy @readonly` | 路由兼容与积分页可用性 |
| `POINT-002` 从用户页进入积分详情 | `@smoke @regression @postdeploy @readonly` | 跨页跳转与详情可见性 |
| `POINT-004` 积分页短字符校验 | `@smoke @regression` | 页面交互校验 |
| `POINT-005` 积分页展示余额与充值流水 | `@write` | 当前通过 API 造数，待切到 fixture 后再升级层级 |
| `USER-003` 用户冻结 / 恢复 | `@smoke @write` | 核心写操作，适合 PR 与专项回归 |
| `LEAD-CRUD` 线索增删改查 | `@write` | 环境污染型写操作，不进入 release regression |

说明：

- `Release Regression` 当前明确不包含线索增删改查。
- `POINT-005` 当前不是只读测试，因此不进入 `@postdeploy`。
- `Write Regression` 中的写操作适合夜间跑或专项回归跑。

## 5. 待补齐用例

- `POINT-005` 的 fixture / seed 版本
  目标是提供一个只读的积分详情展示用例，用于 `@regression @postdeploy @readonly`
- 线索查询
  用于验证 `Release Regression` 阶段的只读线索检索
- 线索详情展示
  用于验证 `Release Regression` 阶段的只读线索详情

## 6. 数据准备策略

### 6.1 推荐方式

- 固定测试账号
- 固定只读测试用户
- fixture API
- seed 脚本
- 预置测试数据库快照

### 6.2 当前约束

- 积分页详情测试不应长期依赖客户端充值页面造数。
- 线索写操作用例必须使用唯一命名或独立测试数据，避免互相污染。
- 用户冻结 / 恢复用例必须使用非 `admin` 测试账号。

## 7. 执行命令

### 7.1 PR Smoke

```bash
cd base-frontend
pnpm --filter @forest/trade-leads-platform-web exec playwright test --grep @smoke
```

### 7.2 Release Regression

```bash
cd base-frontend
pnpm --filter @forest/trade-leads-platform-web exec playwright test --grep @regression
```

### 7.3 Write Regression

```bash
cd base-frontend
pnpm --filter @forest/trade-leads-platform-web exec playwright test --grep @write
```

### 7.4 Post-Deploy Smoke

```bash
cd base-frontend
pnpm --filter @forest/trade-leads-platform-web exec playwright test --grep "@postdeploy.*@readonly|@readonly.*@postdeploy"
```

## 8. 维护规则

- 用例标签变更时，必须同步更新本文件。
- 新增阻塞发布的浏览器用例时，必须先明确测试数据来源。
- 如果某条用例不再适合作为发布门禁，应先降级到 `@write` 或从矩阵中移出，再调整 CI。
