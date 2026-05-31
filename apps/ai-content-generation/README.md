# AI Content Generation App

AI 内容生成 app 空壳。

当前只保留后端启动壳，不建设前端、不接业务模块、不实现 AI 内容生成业务。

## Backend

```bash
mvn -q -pl :forest-app-ai-content-generation -am -DskipTests compile
```

后续如果进入业务建设，通用能力应沉淀到 `business/domains/*` 或 `business/aggregations/*`，app 层只负责装配。

## Docs

- [AI 内容生成 App 文档](docs/README.md)
- [AI 内容生成平台架构总纲](docs/architecture.md)
