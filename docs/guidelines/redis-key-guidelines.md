# Redis KEY 规范

## 1. 目的

这份文档定义 Forest 项目统一的 Redis KEY 命名规范，用于后续短信验证码、登录限流、`submitToken`、checkout 会话、幂等、分布式锁和业务缓存等场景。

当前约定：

- Redis 使用单容器、多 DB index 隔离环境。
- `prod` 使用 DB `0`。
- `local` 使用 DB `1`。
- KEY 不包含 `local` / `prod` 环境前缀。
- 不同业务应用通过业务应用级 app prefix 区分。
- KEY prefix 不使用端级 `appCode`。

## 2. 总格式

统一格式：

```text
forest:{app}:{domain}:{resource}:{id-or-scope...}
```

字段含义：

| 字段 | 说明 |
|---|---|
| `forest` | 系统固定前缀 |
| `app` | 业务应用编码，例如 `cxc-commerce`、`trade-leads` |
| `domain` | 业务域，例如 `auth`、`checkout`、`order`、`payment` |
| `resource` | 资源类型，例如 `sms`、`session`、`submit-token`、`detail` |
| `id-or-scope` | 具体业务标识或更细粒度范围 |

示例：

```text
forest:cxc-commerce:auth:sms:13800138000
forest:cxc-commerce:auth:session:10001
forest:cxc-commerce:checkout:submit-token:ST202605090001
forest:cxc-commerce:lock:order:create:10001
forest:trade-leads:lead:detail:10001
```

## 3. 环境隔离

环境隔离只依赖 Redis DB index：

```text
prod  -> DB 0
local -> DB 1
```

同一个 KEY 可以同时存在于不同 DB index，互不覆盖：

```text
DB 0: forest:cxc-commerce:auth:sms:13800138000 = prod-value
DB 1: forest:cxc-commerce:auth:sms:13800138000 = local-value
```

后续业务服务连接 Redis 时必须显式配置 DB index：

```text
prod  -> redis://:password@172.21.11.156:6379/0
local -> redis://:password@8.136.34.70:6379/1
```

禁止依赖 Redis 默认 DB。启动配置、测试配置和本地配置都必须显式声明 database index。

## 4. app prefix 规则

`app` 使用业务应用级编码：

```text
cxc-commerce
trade-leads
```

同一个业务应用下的 PC、小程序、H5、APP 默认共享缓存空间。

默认不使用端级 `appCode`：

```text
cxc-commerce-buyer-wechat-miniapp
cxc-commerce-platform-web
trade-leads-platform-web
trade-leads-miniapp
```

如果某个 KEY 确实需要按端区分，再在后续段增加 `clientType`：

```text
forest:cxc-commerce:auth:session:wechat-miniapp:{sessionId}
forest:cxc-commerce:auth:session:platform-web:{sessionId}
```

## 5. 命名风格

静态段统一使用：

- 小写。
- `kebab-case`。
- 冒号分隔。
- 从大范围到小范围排列。

允许：

```text
submit-token
checkout-session
payment-notify
rate-limit
```

不允许：

```text
submitToken
checkout_session
SubmitToken
```

KEY 不允许出现空段：

```text
forest:cxc-commerce::sms
forest:cxc-commerce:auth::13800138000
```

## 6. 敏感标识

当前决策：允许手机号、openid、token 等明文进入 KEY，方便排查。

示例：

```text
forest:cxc-commerce:auth:sms:13800138000
forest:cxc-commerce:wechat:openid:o_xxx
```

操作约束：

- 禁止执行 `FLUSHALL`。
- 执行 `FLUSHDB` 前必须确认当前 DB index。
- 生产排查时必须先确认当前连接的 DB index。
- 线上问题排查优先使用精确 KEY 查询，不默认使用全量扫描。

## 7. TTL 规则

除非是明确的长期缓存，否则 Redis KEY 必须设置 TTL。

建议默认 TTL：

| 场景 | TTL |
|---|---|
| 短信验证码 | 5 分钟 |
| 登录失败限流 | 15 分钟 |
| `submitToken` | 30 分钟 |
| `checkoutSession` | 30 分钟 |
| 幂等请求结果 | 24 小时 |
| 支付回调幂等 | 24 小时 |
| 分布式锁 | 5-30 秒 |
| 普通详情缓存 | 5-30 分钟 |

永久 KEY 必须单独说明原因，不允许默认永久保存。

## 8. 推荐 KEY

### 8.1 Auth

```text
forest:{app}:auth:sms:{phone}
forest:{app}:auth:sms:send-limit:{phone}
forest:{app}:auth:login-fail:{accountType}:{identifier}
forest:{app}:auth:session:{sessionId}
forest:{app}:auth:refresh-jti:{jti}
```

示例：

```text
forest:cxc-commerce:auth:sms:13800138000
forest:cxc-commerce:auth:login-fail:phone:13800138000
forest:cxc-commerce:auth:session:10001
```

### 8.1.1 Verification

验证码、发送冷却、每日发送上限和一次性 ticket 统一放在 `verification` domain：

```text
forest:{app}:verification:sms-code:{scene}:{phone}
forest:{app}:verification:sms-attempt:{scene}:{phone}
forest:{app}:verification:sms-send-limit:{scene}:{phone}
forest:{app}:verification:sms-daily-limit:{scene}:{phone}:{yyyyMMdd}
forest:{app}:verification:ticket:{scene}:{ticketNo}
```

含义：

| KEY | 用途 |
|---|---|
| `sms-code` | 存验证码 hash，TTL 默认 5 分钟，校验成功后删除 |
| `sms-attempt` | 记录输错次数，防暴力猜测 |
| `sms-send-limit` | 发送冷却，默认 60 秒内不能重复发送 |
| `sms-daily-limit` | 同一手机号同一场景每日发送上限 |
| `ticket` | 验证通过后的短期一次性凭证，用于修改手机号、找回密码、支付确认等分步骤场景 |

示例：

```text
forest:cxc-commerce:verification:sms-code:login:13800138000
forest:cxc-commerce:verification:sms-attempt:login:13800138000
forest:cxc-commerce:verification:sms-send-limit:login:13800138000
forest:cxc-commerce:verification:sms-daily-limit:login:13800138000:20260514
forest:cxc-commerce:verification:ticket:change-phone:VT202605140001
```

### 8.2 Checkout

```text
forest:{app}:checkout:session:{checkoutSessionId}
forest:{app}:checkout:submit-token:{submitToken}
forest:{app}:checkout:fingerprint:{requestFingerprint}
```

示例：

```text
forest:cxc-commerce:checkout:session:CS202605090001
forest:cxc-commerce:checkout:submit-token:ST202605090001
```

### 8.3 Order

```text
forest:{app}:order:idempotency:{idempotencyKey}
forest:{app}:order:create-result:{submitToken}
forest:{app}:lock:order:create:{userId}
forest:{app}:lock:order:pay:{orderId}
```

### 8.4 Payment

```text
forest:{app}:payment:notify:{channel}:{notifyId}
forest:{app}:payment:pay-lock:{paymentOrderId}
forest:{app}:payment:reconcile-lock:{reconcileDate}
```

示例：

```text
forest:cxc-commerce:payment:notify:wechat:420000001
forest:cxc-commerce:payment:notify:alipay:202605090001
```

### 8.5 Cache

```text
forest:{app}:cache:{domain}:{resource}:{id}
```

示例：

```text
forest:cxc-commerce:cache:product:spu:10001
forest:cxc-commerce:cache:category:tree:user
forest:trade-leads:cache:lead:detail:10001
```

### 8.6 Lock

分布式锁统一放在 `lock` domain：

```text
forest:{app}:lock:{domain}:{action}:{id}
```

示例：

```text
forest:cxc-commerce:lock:inventory:deduct:sku10001
forest:cxc-commerce:lock:payment:split:paymentOrder10001
```

## 9. 后续实现要求

Java 接入 Redis 时，必须通过 `forest-starter-redis` 统一封装 KEY 构造、TTL 和基础 Redis 操作，不允许业务代码手拼字符串。

当前统一入口：

```text
RedisKeyFactory
RedisKey
ForestRedisClient
ForestRedisJsonClient
ForestRedisRateLimiter
ForestRedisLockClient
ForestRedisTtlPolicy
```

调用目标：

```java
redisKeys.authSms(phone)
redisKeys.checkoutSubmitToken(submitToken)
redisKeys.lock("order", "create", userId)
```

配置来源：

```text
forest.redis.app-code=cxc-commerce
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.database=1
```

其中：

- `forest.redis.app-code` 负责 KEY prefix。
- `spring.data.redis.host` / `spring.data.redis.port` 负责连接 Redis 服务地址。
- `spring.data.redis.database` 负责 local/prod DB index。
- `starter-redis` 内部可以使用 `StringRedisTemplate` 和 Jackson `ObjectMapper`，业务模块不直接注入 `StringRedisTemplate` / `RedisTemplate`。
- `commons-pool2`、Spring Cache、Redisson 暂不引入；后续连接池、读模型缓存、强锁场景再单独评估。

## 10. 测试要求

`RedisKeyFactory` 后续实现时必须覆盖：

- 生成的 KEY 必须以 `forest:{app}:` 开头。
- 静态段必须是小写 `kebab-case`。
- `app` 必须是业务应用级编码。
- 不允许生成空段。
- 不允许业务代码直接拼接 Redis KEY。

示例断言：

```text
authSms("13800138000")
=> forest:cxc-commerce:auth:sms:13800138000

checkoutSubmitToken("ST202605090001")
=> forest:cxc-commerce:checkout:submit-token:ST202605090001

lock("order", "create", "10001")
=> forest:cxc-commerce:lock:order:create:10001
```
