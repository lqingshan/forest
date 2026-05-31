# Forest CI/CD 总览

Purpose: 说明 Forest 项目从 PR 到生产发布的 CI/CD 总体流程。  
Scope: 当前覆盖 `trade-leads` app 的测试门禁、构建、staging 验证与发布入口。  
Owner: 工程效率 / 应用负责人联合维护。  
Last Updated: 2026-04-15  
Related Docs:

- [branch-and-triggers.md](./branch-and-triggers.md)
- [quality-gates.md](./quality-gates.md)
- [browser-quality-gates.md](./browser-quality-gates.md)

## 1. 目标

Forest 的 CI/CD 目标是：

- 让问题尽量在 PR 阶段暴露，而不是在发布阶段暴露
- 让进入 `staging` 的构建已经通过主要质量门禁
- 让进入 `production` 的构建是同一份已经验证过的产物
- 让失败能够快速阻断并回滚

## 2. 总体发布路径

```text
feature branch
  -> PR Check
  -> merge main
  -> build and publish artifacts/images
  -> deploy staging
  -> release regression
  -> manual approval
  -> deploy production
  -> post-deploy smoke
```

## 3. 当前推荐模块

- 分支与触发规则：定义什么事件触发什么流水线
- 质量门禁：定义 PR、候选版本、生产发布后的检查集合
- 浏览器质量门禁：定义 Playwright 在不同发布阶段的职责

## 4. 当前推荐发布节奏

### 4.1 PR 阶段

目标：回答“这次改动能不能合并”。

执行：

- 前端单测
- 后端快测
- 前端构建检查
- 浏览器冒烟

### 4.2 Main 合并后

目标：回答“候选版本是否值得进入 staging”。

执行：

- 构建后端产物
- 构建前端产物
- 推送版本化产物或镜像
- 自动部署到 staging

### 4.3 Staging 阶段

目标：回答“这份候选版本能不能发布”。

执行：

- release regression
- 浏览器只读回归
- PostgreSQL 特性验证

### 4.4 Production 阶段

目标：回答“生产刚发布后是否可用”。

执行：

- post-deploy smoke
- 失败即阻断或回滚

## 5. 当前文档边界

这组文档先聚焦质量门禁与触发规则，不在本阶段展开：

- 镜像仓库细节
- 部署平台实现细节
- 秘钥管理平台细节
- 具体云厂商能力差异

这些内容后续如需要，可补到新的 `build-and-package.md`、`deploy-staging.md`、`deploy-production.md`、`rollback.md` 模块。
