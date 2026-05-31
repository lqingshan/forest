# Forest 分支策略与触发规则

Purpose: 定义 Forest 当前推荐的分支策略，以及不同事件触发的 CI/CD 流水线。  
Scope: 当前聚焦 `trade-leads` app 的 monorepo 工作流。  
Owner: 工程效率 / 仓库管理员联合维护。  
Last Updated: 2026-04-15  
Related Docs:

- [README.md](./README.md)
- [quality-gates.md](./quality-gates.md)

## 1. 当前推荐策略

Forest 当前推荐使用：`trunk-based development + short-lived branches`。

也就是：

- 日常开发基于短生命周期功能分支
- 通过 PR 合并回 `main`
- `main` 始终保持可发布
- 正式版本通过 tag 或审批动作触发生产发布

推荐原因：

- monorepo 更适合降低长期分支漂移
- 当前团队规模与模块复杂度更适合快速集成
- 对浏览器自动化与 staging 验证的衔接更自然

## 2. 当前建议分支命名

- `main`：唯一长期主干分支
- `feature/<topic>`：新功能分支
- `fix/<topic>`：普通缺陷修复分支
- `hotfix/<topic>`：紧急线上修复分支
- `release/<topic>`：仅在确有冻结窗口时短期存在，默认不作为常规流程

## 3. 什么事件触发什么流水线

### 3.1 Pull Request 事件

触发时机：

- 新建 PR
- PR 有新提交
- PR 重新触发检查

触发流水线：

- 前端单测
- 后端快测
- 前端构建检查
- 浏览器 `@smoke`

回答的问题：

- 这次改动能不能合并

### 3.2 Merge To Main 事件

触发时机：

- PR 合并到 `main`

触发流水线：

- 构建后端产物
- 构建前端产物
- 推送版本化产物 / 镜像
- 自动部署 staging

回答的问题：

- 这次主干构建是否可以成为候选版本

### 3.3 Staging Deploy Complete 事件

触发时机：

- staging 部署完成

触发流水线：

- 浏览器 `@regression`
- PostgreSQL 特性验证

回答的问题：

- 这份候选版本能不能进入生产审批

### 3.4 Schedule 事件

触发时机：

- 每晚固定时间
- 每周固定时间

触发流水线：

- `@write` 浏览器回归
- 更重的数据库兼容性测试
- 长链路专项回归

回答的问题：

- 非阻塞但高风险的流程是否仍然稳定

### 3.5 Release Tag 或 Manual Approval 事件

触发时机：

- 创建版本 tag，例如 `v1.2.3`
- 人工点击批准生产发布

触发流水线：

- 部署 production
- 运行 `@postdeploy @readonly`

回答的问题：

- 生产是否已成功切换到可用状态

## 4. 当前推荐触发表

| 事件 | 推荐触发 | 目标 |
| --- | --- | --- |
| PR opened / synchronized | PR Check | 合并门禁 |
| merge to `main` | Build + Staging Deploy | 生成候选版本 |
| staging deploy complete | Release Regression | 发布前验证 |
| nightly schedule | Write Regression | 非阻塞重回归 |
| release approval / version tag | Production Deploy | 正式发布 |
| production deploy complete | Post-Deploy Smoke | 发布后确认 |

## 5. 常见分支策略

### 5.1 Trunk-Based

特点：

- 只有一个长期主干
- 功能分支短生命周期
- 依赖强自动化测试

适用：

- monorepo
- 迭代频繁团队
- 强调持续集成

这是 Forest 当前推荐方案。

### 5.2 GitFlow

特点：

- `main`、`develop`、`release/*`、`hotfix/*` 同时存在
- 流程更重，分支更多

适用：

- 发布窗口严格
- 版本并行维护较多
- 组织流程偏重审批

不作为 Forest 当前首选方案。

### 5.3 Release Branch

特点：

- 平时走 trunk-based
- 发版前短期开 `release/*`

适用：

- 某些版本需要冻结
- 需要一边修问题一边继续主干开发

Forest 可以在将来有冻结窗口时引入，但不建议作为日常默认流程。

### 5.4 Environment Branch

特点：

- 用 `dev`、`staging`、`prod` 分支代表环境

风险：

- 分支容易与真实部署状态脱节
- 回溯和 cherry-pick 成本高

不推荐作为 Forest 的主策略。

## 6. 当前维护规则

- `main` 必须始终可发布。
- 长生命周期开发分支不应成为默认工作模式。
- 任何新增触发器都必须在本文件中登记对应事件、流水线和目标。
- 如果触发规则影响质量门禁，必须同步更新 [quality-gates.md](./quality-gates.md)。
