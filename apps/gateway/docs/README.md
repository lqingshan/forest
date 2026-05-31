# Gateway 文档地图

Gateway 是 Forest 当前的 Nginx 入口 app，用于承载反向代理、公共请求头透传和部署入口。

## 模块定位

`apps/gateway` 不是业务系统，它是运行入口和流量转发层。它不拥有业务接口、不实现认证逻辑、不保存业务状态。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| Nginx gateway | 提供统一反向代理入口 |
| proxy headers | 维护请求头透传和上游识别配置 |
| Dockerfile | 提供 gateway 镜像构建入口 |
| app 路由转发 | 面向后端 app 或前端静态资源的转发配置 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 业务认证判断 | 认证和授权由后端 app / business 模块完成 |
| 业务路由编排 | 不在 gateway 中表达业务流程 |
| 数据存储 | gateway 不持久化业务数据 |

## 关键技术点

- 核心配置位于 `nginx/nginx.conf`。
- 公共代理请求头位于 `nginx/snippets/proxy-headers.conf`。
- gateway 可以参与环境部署，但不应成为业务规则承载层。
- 路由变更需要结合具体 app 的 backend/frontend 入口一起审阅。

## 职责边界

| 层级 | 职责 |
|---|---|
| `apps/gateway` | Nginx 配置、Dockerfile、入口转发 |
| `apps/*/backend` | 真实业务接口和认证授权 |
| `apps/*/clients/*` | 前端页面和静态资源 |
| `deploy/*` | 环境部署脚本和运行拓扑 |

## 推荐阅读顺序

1. 本文档地图。
2. `nginx/nginx.conf`：主 Nginx 配置。
3. `nginx/snippets/proxy-headers.conf`：代理请求头配置。
4. 根部署脚本和具体 app README。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| `nginx/nginx.conf` | gateway 主配置 | 已有 |
| `nginx/snippets/proxy-headers.conf` | 公共代理请求头 | 已有 |
| `Dockerfile` | gateway 镜像构建入口 | 已有 |
| `runtime.md` | 运行和部署说明 | 待补 |

## 当前状态

- 当前有 Nginx 配置和 Dockerfile。
- 还缺少独立 runtime 文档；后续应补充本地/生产环境 gateway 启停和路由变更流程。

## 维护规则

- gateway 文档只写入口层和转发层内容。
- app 业务路径、认证权限、接口语义必须回到对应 app/module 文档。
- 修改 Nginx 配置时，应同步说明影响的 app、路径和上游服务。
