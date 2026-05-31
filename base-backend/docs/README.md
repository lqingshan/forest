# Base Backend 文档地图

Base Backend 是后端基础设施层，提供跨 app、跨 business 模块复用的 starter 能力。

## 模块定位

`base-backend` 只承载技术基础设施和通用框架适配，不依赖具体 business domain，也不实现 app 业务逻辑。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| common | 通用异常、响应、审计基类和基础工具 |
| auth | token 校验、认证上下文和端准入基础能力 |
| redis | Redis 配置和基础接入 |
| sms | 短信供应商技术封装 |
| wechat | 微信平台技术能力封装 |
| object-storage | 对象存储技术封装 |
| ocr | OCR 技术能力封装 |
| carrier-auth | 运营商号码认证技术封装 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 业务实体 | 业务模型属于 business domain |
| app 流程 | app 装配和流程属于 apps |
| 跨域业务编排 | 属于 business aggregations |

## 关键技术点

- starter 应提供可注入、可配置、可替换的技术能力。
- starter 不应依赖 business module，避免基础层反向依赖业务层。
- 认证 starter 只表达通用 token 和 principal，不写入企业业务语义。
- 供应商配置、密钥和证书不能写入文档明文。

## 职责边界

| 层级 | 职责 |
|---|---|
| base-backend | 技术 starter 和基础框架能力 |
| business/domains | 业务实体、领域服务、repository 和业务规则 |
| business/aggregations | 跨业务域编排 |
| apps | app 装配、启动类、环境配置和端入口 |

## 推荐阅读顺序

1. 本文档地图。
2. [carrier-auth.md](./carrier-auth.md)：APP 本机号一键登录后端技术抽象。
3. [../starter-common](../starter-common)：通用异常、响应和基础对象。
4. [../starter-auth](../starter-auth)：token、principal 和认证拦截基础能力。
5. [../../docs/guidelines/development-guidelines.md](../../docs/guidelines/development-guidelines.md)：统一开发规范。

## 文档清单

| Starter | 内容 | 状态 |
|---|---|---|
| `starter-common` | 通用异常、响应、审计基类 | 已有代码，文档待补 |
| `starter-auth` | token 校验和认证上下文 | 已有代码，文档待补 |
| `starter-redis` | Redis 接入 | 已有代码，文档待补 |
| `starter-sms` | 短信供应商封装 | 已有代码，文档待补 |
| `starter-wechat` | 微信能力封装 | 已有代码，文档待补 |
| `starter-object-storage` | 对象存储封装 | 已有代码，文档待补 |
| `starter-ocr` | OCR 封装 | 已有代码，文档待补 |
| `starter-carrier-auth` | 运营商号码认证封装，详见 [carrier-auth.md](./carrier-auth.md) | 已有 |

## 当前状态

- 多个 starter 已存在并被 app/business 复用。
- 仍缺少 starter 级详细使用文档，后续可逐个补充。

## 维护规则

- base-backend 不写业务规则。
- 新增 starter 需要说明配置项、暴露 Bean、异常语义和使用边界。
