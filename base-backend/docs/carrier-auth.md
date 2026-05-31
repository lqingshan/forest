# 号码认证技术抽象

本文档沉淀 APP 本机号一键登录所需的后端技术抽象。

相关文档：

- [Base Backend 文档地图](./README.md)
- [User 认证架构](../../business/domains/user/docs/auth-architecture.md)
- [CXC 移动端登录方案](../../apps/cxc-commerce/docs/mobile-auth-design.md)

## 1. 模块定位

号码认证属于技术能力，不属于 user domain 的核心业务模型。

业务只表达：

```text
通过 carrierToken 解析可信手机号
```

供应商 SDK、签名、endpoint、mock/disabled 实现放在：

```text
base-backend/starter-carrier-auth
```

## 2. 后端抽象

业务模块只依赖稳定接口：

```text
CarrierAuthClient
CarrierPhoneResolveRequest
CarrierPhoneResolveResult
```

处理方向：

```text
carrierToken
-> CarrierAuthClient.resolvePhone
-> 标准化手机号
-> 交给 user domain 创建或复用 account/user/session
```

## 3. 实现

| 实现 | 作用 |
|---|---|
| `AliyunCarrierAuthClient` | 调用阿里云号码认证能力 |
| `MockCarrierAuthClient` | 本地或测试环境返回 mock 手机号 |
| `DisabledCarrierAuthClient` | 明确关闭本机号一键登录 |

## 4. 配置

```yaml
forest:
  carrier-auth:
    provider: ${FOREST_CARRIER_AUTH_PROVIDER:disabled}
    mock-phone: ${FOREST_CARRIER_AUTH_MOCK_PHONE:+8613800138000}
    aliyun:
      endpoint: ${FOREST_CARRIER_AUTH_ALIYUN_ENDPOINT:dypnsapi.aliyuncs.com}
      access-key-id: ${FOREST_CARRIER_AUTH_ALIYUN_ACCESS_KEY_ID:}
      access-key-secret: ${FOREST_CARRIER_AUTH_ALIYUN_ACCESS_KEY_SECRET:}
```

约束：

- `access-key-id` / `access-key-secret` 只能通过环境变量或密钥系统配置。
- 文档和代码示例不得写入真实密钥。
- `disabled` 必须返回明确错误，不能静默降级成不可信手机号。

## 5. 异常语义

常见错误：

| 错误 | 说明 |
|---|---|
| `CARRIER_UNAVAILABLE` | 当前设备或环境不支持本机号能力 |
| `TOKEN_EXPIRED` | carrierToken 已过期 |
| `AUTH_FAILED` | 供应商解析失败或拒绝 |
| `PROVIDER_DISABLED` | 服务端关闭号码认证 |
| `UNKNOWN_ERROR` | 其他未知供应商错误 |

前端或 app 可以根据错误降级到短信验证码登录。

## 6. 边界

- starter 不创建 user。
- starter 不签发 token。
- starter 不写业务登录日志。
- starter 不依赖 business 模块。
- app 是否启用一键登录由 app 配置和端能力决定。
