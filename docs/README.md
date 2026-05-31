# Forest Docs

本目录用于沉淀 Forest 的全局架构、开发规范、跨模块方案、CI/CD 规则和文档导航。

具体 app/module 的业务细节应放在对应模块自己的 `docs/README.md` 和同级文档中。根目录 `docs` 负责全局共识，不替单个模块承载长期业务说明。

建议阅读顺序：

1. [documentation-map.md](./documentation-map.md)：全局文档地图，进入各 app/module 内部文档地图。
2. [development-guidelines.md](./guidelines/development-guidelines.md)：统一开发规范入口。
3. [architecture.md](./architecture/architecture.md)：项目分层、模块边界和 app / domain / aggregation 职责。
4. [database-guidelines.md](./guidelines/database-guidelines.md)：数据库设计、Flyway migration、软删除、唯一约束和 SQL 规范。
5. [cicd/README.md](./cicd/README.md)：CI/CD、分支触发器和质量门禁规范。
6. 各 app/module 自己的 `docs/README.md`：模块定位、需求范围、关键技术点和文档清单。

## 目录分组

### 核心规范

| 目录或文件 | 定位 |
|---|---|
| `documentation-map.md` | 全局文档地图，链接到每个 app/module 的内部文档地图 |
| `guidelines/development-guidelines.md` | 当前统一开发规范入口，跨模块规范优先维护在这里 |
| `architecture/architecture.md` | 架构总览和模块边界 |
| `guidelines/app-guidelines.md` | app 建壳、文档结构和新 app 前置检查 |
| `guidelines/frontend-web-guidelines.md` | PC Web app、企业工作台页面、权限展示和前端状态 |
| `guidelines/frontend-component-reuse.md` | 前端组件复用、组件地图和 review 检查项 |
| `guidelines/database-guidelines.md` | 数据库专项规范 |
| `guidelines/redis-key-guidelines.md` | Redis KEY、DB index、TTL 和接入规范 |

### 模块专项设计

| 目录或文件 | 定位 |
|---|---|
| `../business/domains/access/docs/rbac-guidelines.md` | Access、RBAC、权限点、角色和平台权限边界 |
| `../business/domains/organization/docs/workspace-guidelines.md` | organization、企业工作台上下文和企业认证 Gate |
| `../business/domains/file/docs/file-module-design.md` | 文件模块、OSS、上传下载和多文件上传交互规范 |
| `../business/domains/user/docs/auth-architecture.md` | user/account/auth/session/token 认证边界 |
| `../apps/cxc-commerce/docs/mobile-auth-design.md` | CXC Android/iOS + H5 移动端登录方案 |
| `../base-backend/docs/carrier-auth.md` | APP 本机号一键登录后端技术抽象 |

### 业务方案和专项设计

| 目录或文件 | 定位 |
|---|---|
| `designs/async-task-system-design.md` | 异步任务系统设计 |

### App 专属文档

| 目录或文件 | 定位 |
|---|---|
| `../apps/trade-leads/docs/README.md` | Trade Leads app 内部文档地图 |
| `../apps/cxc-commerce/docs/README.md` | CXC Commerce app 内部文档地图 |
| `../apps/attendance/docs/README.md` | Attendance app 内部文档地图 |
| `../apps/ai-content-generation/docs/README.md` | AI 内容生成 app 内部文档地图 |
| `../apps/gateway/docs/README.md` | Gateway app 内部文档地图 |

### 模块文档地图

| 目录或文件 | 定位 |
|---|---|
| `../business/domains/*/docs/README.md` | 单业务域模块的内部文档地图 |
| `../business/aggregations/*/docs/README.md` | 跨业务域编排模块的内部文档地图 |
| `../base-backend/docs/README.md` | 后端基础设施文档地图 |
| `../base-frontend/docs/README.md` | 前端基础设施文档地图 |

### 目录资产

| 目录或文件 | 定位 |
|---|---|
| `cicd/` | 分支、触发器、质量门禁、浏览器验证规范 |
| `generated/` | AI 或工具生成的参考图片，不作为唯一事实源 |
| `presentations/` | 对外方案、汇报材料、PPT 和 PDF |

## 维护规则

- 通用开发规则优先写入 [development-guidelines.md](./guidelines/development-guidelines.md)。
- 架构边界变更优先写入 [architecture.md](./architecture/architecture.md)。
- 单个 app/module 的长期文档优先放在自己的 `docs/README.md` 和同级文档中。
- 新增 app/domain/aggregation/base package 时，必须同步补充内部文档地图并登记到 [documentation-map.md](./documentation-map.md)。
- 数据库、Redis、文件、CI/CD 等专项规则写入对应专项文档，并在总规范保留链接。
- 方案讨论类文档如果变成长期规范，应提炼进总规范、专项规范或对应模块文档。
- 生成图、PPT、PDF 不是唯一事实源；与 Markdown 文档冲突时，以 Markdown 文档为准。
- 不提交 `.DS_Store`、本地预览产物和临时截图。
