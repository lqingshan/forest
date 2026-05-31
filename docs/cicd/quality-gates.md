# Forest 质量门禁

Purpose: 定义 Forest 发布流程中的质量门禁与放行规则。  
Scope: 当前覆盖 `trade-leads` app 的前端、后端与浏览器自动化测试。  
Owner: 工程效率 / 应用负责人联合维护。  
Last Updated: 2026-04-15  
Related Docs:

- [README.md](./README.md)
- [browser-quality-gates.md](./browser-quality-gates.md)

## 1. 原则

- 先在 PR 阶段拦截大部分问题，再在 staging 阶段做版本候选验证。
- 发布前验证的是“候选版本”，不是开发过程中的某次本地运行结果。
- 生产发布后只运行只读、极小集合的 smoke，不做破坏性写操作。
- 测试数据准备应独立于被测主流程，避免测试与未完成业务链路耦合。

## 2. 门禁层级

### 2.1 PR Gate

目标：判断改动能否合并。

要求：

- 前端单测通过
- 后端快测通过
- 前端构建通过
- 浏览器冒烟通过

当前建议命令：

```bash
cd base-frontend
pnpm install --frozen-lockfile
pnpm --filter @forest/user test
pnpm --filter @forest/point test
pnpm --filter @forest/lead test
pnpm --filter @forest/user-point test
pnpm --filter @forest/trade-leads-platform-web build
pnpm --filter @forest/trade-leads-platform-web exec playwright test --grep @smoke

cd ../base-backend
mvn -pl ../apps/trade-leads/backend -am test
```

### 2.2 Release Candidate Gate

目标：判断候选版本能否进入生产发布审批。

要求：

- PR Gate 已通过
- staging 部署成功
- PostgreSQL 特性测试通过
- 浏览器 `@regression` 通过

### 2.3 Write Regression Gate

目标：验证高风险写操作流程，但不将其纳入正式发版阻塞项。

用途：

- 夜间回归
- 特定预发窗口
- 大版本前专项回归

当前建议包括：

- 用户冻结 / 恢复
- 线索新增 / 编辑 / 删除

### 2.4 Post-Deploy Gate

目标：验证生产刚发布后没有出现基础事故。

要求：

- 只运行 `@postdeploy @readonly`
- 失败立即告警
- 满足自动回滚条件时应触发回滚流程

## 3. 当前放行规则

### 3.1 PR 放行

以下任一失败，PR 不允许合并：

- 前端单测失败
- 后端快测失败
- 前端构建失败
- 浏览器冒烟失败

### 3.2 Staging 放行

以下任一失败，不允许进入生产审批：

- staging 部署失败
- PostgreSQL 特性测试失败
- 浏览器回归失败

### 3.3 Production 放行

以下任一失败，认为生产发布不成功：

- post-deploy smoke 失败
- 核心健康检查失败

## 4. 当前测试数据策略

- 浏览器测试必须优先使用固定测试账号、fixture API、seed 脚本或预置数据。
- 不要把“测试数据准备”绑定到未完成业务页面。
- 不要让积分测试强依赖客户端充值页面完成度。
- 生产 smoke 只能使用只读测试数据或专用测试租户数据。

## 5. 当前最小可执行集合

### 5.1 PR Smoke

- 登录成功
- 用户冻结 / 恢复
- 登出与路由守卫
- 积分页输入校验
- 用户页跳转到积分详情

### 5.2 Release Regression

- PR Smoke 全部用例
- 旧积分路由跳转
- 积分页详情展示
- 用户查询结果展示
- 线索查询
- 线索详情展示

### 5.3 Write Regression

- 用户冻结 / 恢复
- 线索新增 / 编辑 / 删除

### 5.4 Post-Deploy Smoke

- 登录成功
- 用户页可打开并完成只读查询
- 积分页可打开并查看固定测试用户详情
- 线索页可打开并完成只读查询
