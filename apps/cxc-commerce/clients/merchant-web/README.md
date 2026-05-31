# CXC Commerce Merchant Web

商家端 PC 后台。

## 职责

- 商家登录后的 PC 工作台。
- 当前已接入手机号密码登录、企业列表、企业创建、企业认证提交、部门管理、员工管理。
- 后续承载商品管理、库存管理、订单履约、售后处理、结算查看等商家能力。

## 常用命令

```bash
pnpm --dir /Users/lgd/project/forest/base-frontend --filter @forest/cxc-commerce-merchant-web build
pnpm --dir /Users/lgd/project/forest/base-frontend build:cxc-merchant-web
```

## 边界

- 商家端只使用通用认证会话结果。
- 企业、部门、员工归属 `business/domains/organization`。
- 店铺、商品、订单、权限等业务身份和授权逻辑后续独立建模，不放入通用账号中心。
