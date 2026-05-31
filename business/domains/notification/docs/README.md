# Notification 文档地图

Notification 是通知发送记录和通知审计领域模块。

## 模块定位

`business/domains/notification` 负责记录通知发送行为和供应商结果。它不决定业务是否允许发送，也不直接表达验证码校验逻辑。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 短信发送日志 | 记录手机号、场景、模板、供应商响应和状态 |
| 通知审计 | 为登录、注册、验证码等流程提供发送审计 |
| 供应商协作 | 与 `starter-sms` 配合完成短信发送 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 验证码状态 | 属于 verification domain |
| 用户创建或登录 | 属于 user domain |
| 复杂消息中心 | 站内信、推送订阅等后续再设计 |

## 关键技术点

- 通知记录应可追踪业务场景、接收人、模板、发送状态和供应商响应。
- 供应商技术能力在 `base-backend/starter-sms`，notification 只沉淀业务审计。
- 验证码是否有效由 verification 判断，notification 不校验验证码。

## 职责边界

| 层级 | 职责 |
|---|---|
| notification domain | 通知发送记录和审计 |
| starter-sms | 短信供应商 SDK 和发送能力 |
| verification domain | 验证码状态和校验 |
| apps | 选择具体业务场景和用户交互 |

## 推荐阅读顺序

1. 本文档地图。
2. `backend/src/main/java/com/forest/notification/sms`：短信通知相关实现。
3. [../../../../base-backend/docs/README.md](../../../../base-backend/docs/README.md)：后端 starter 入口。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| `sms.md` | 短信日志、模板和供应商说明 | 待补 |

## 当前状态

- 短信通知审计能力已拆为独立 domain。
- 后续如引入站内信、推送、邮件，应继续明确与业务流程的边界。

## 维护规则

- 通知发送结果必须可审计。
- 不在 notification 中实现验证码业务状态。
