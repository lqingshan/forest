# AI 内容生成 App 文档地图

AI 内容生成 app 当前是空壳，用于预留独立 app 入口和后续 AI 内容生成业务规划。

## 模块定位

`apps/ai-content-generation` 是独立 app 壳。当前只保留后端启动壳和文档，不建设前端、不接业务模块、不实现 AI 内容生成业务。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 后端 app 壳 | 保留独立启动入口和 Maven artifact |
| 文档入口 | 保存 AI 内容生成 app 的架构构想和后续资料 |
| 后续预留 | 为 AI 内容生成 domain、队列、素材、任务等能力预留 app 边界 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 前端应用 | 当前不建设 web/mobile 端 |
| AI 业务域 | 暂不实现 prompt、任务、素材、生成记录等模型 |
| 模型供应商接入 | 暂不接 OpenAI 或其他模型 API |

## 关键技术点

- app 层只负责装配，不直接沉淀通用 AI 内容生成能力。
- 后续 AI 任务、内容资产、生成记录等模型应进入 `business/domains/*`。
- 如果出现跨多个业务域的生成流程，再进入 `business/aggregations/*`。
- 当前文档中的 PPT 和架构图是方案资产，不代表已实现能力。

## 职责边界

| 层级 | 职责 |
|---|---|
| `apps/ai-content-generation/backend` | 空后端启动壳 |
| future domain | AI 任务、素材、内容、模型调用等核心业务 |
| app docs | 架构构想、PPT、阶段性规划 |

## 推荐阅读顺序

1. 本文档地图。
2. [architecture.md](./architecture.md)：AI 内容生成平台架构总纲。
3. [diagrams/](./diagrams/)：app 专属架构图。
4. [presentations/](./presentations/)：app 专属 PPT。

## 文档清单

| 文档 | 说明 |
|---|---|
| [architecture.md](./architecture.md) | AI 内容生成平台架构总纲 |
| [diagrams/](./diagrams/) | app 专属架构图 |
| [presentations/](./presentations/) | app 专属 PPT |

## 当前状态

- 当前是 app 空壳和文档资产。
- 尚未建设 AI 内容生成业务 domain。
- 进入真实开发前，需要先补需求范围、模型边界和任务流程设计。

## 维护规则

- app 专属方案、PPT、图表放在本目录。
- 通用 AI 业务能力启动后，应拆到对应 business domain docs。
- 不把模型供应商密钥、私有 prompt 或运行时配置写入文档。
