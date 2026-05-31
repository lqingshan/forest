# Verification 文档地图

Verification 是验证码、校验票据、发送冷却和防刷控制领域模块。

## 模块定位

`business/domains/verification` 负责“证明某个手机号或身份验证动作通过了校验”，不负责创建用户、不负责签发 token、不直接发送短信供应商请求。

## 需求范围

### 本期范围

| 范围 | 说明 |
|---|---|
| 手机验证码 | 发送、校验、过期和错误次数控制 |
| 冷却限制 | 同一手机号或场景的发送冷却 |
| verification ticket | 校验通过后的短期票据 |
| Redis 状态 | 验证码、错误次数、冷却等临时状态 |

### 暂不包含

| 暂不包含 | 说明 |
|---|---|
| 用户创建 | 属于 user domain |
| session/token | 属于 user auth/session |
| 短信供应商调用 | 由 notification / starter-sms 协作 |

## 关键技术点

- 验证码状态应使用 Redis，并设置明确 TTL。
- 验证通过后可生成一次性 verification ticket，供后续注册、登录或绑定流程使用。
- 验证码场景必须区分用途，避免不同业务互相复用校验结果。
- 发送日志和供应商响应不归 verification 持久化，归 notification 记录。

## 职责边界

| 层级 | 职责 |
|---|---|
| verification domain | 验证码生成、校验、冷却、ticket |
| notification domain | 短信发送记录和供应商结果 |
| starter-sms | 短信供应商技术封装 |
| user domain | 登录、注册、绑定账号 |

## 推荐阅读顺序

1. 本文档地图。
2. `backend/src/main/java/com/forest/verification`：验证码服务实现。
3. [../../../../docs/guidelines/redis-key-guidelines.md](../../../../docs/guidelines/redis-key-guidelines.md)：Redis KEY 与 TTL 规范。

## 文档清单

| 文档 | 内容 | 状态 |
|---|---|---|
| 本文档 | 模块地图、需求范围、关键技术点 | 已有 |
| `api.md` | 验证码发送和校验接口 | 待补 |
| `scenarios.md` | 登录、绑定、注册等场景说明 | 待补 |

## 当前状态

- 验证码能力已作为独立 domain 存在。
- 仍需补充场景级文档，说明不同 app/端如何使用验证码。

## 维护规则

- 新增验证码场景时必须说明用途、TTL、冷却和校验后动作。
- 不在 verification 中创建 user 或 session。
