# CXC Commerce Platform Web

平台端 PC 后台。

## 职责

- 平台运营和管理后台。
- 当前已接入平台密码登录、企业认证待审核列表、审核通过、审核驳回。
- 后续承载商家管理、类目管理、商品监管、订单监管、运营活动、支付分账、对账等平台能力。

## 常用命令

```bash
pnpm --dir /Users/lgd/project/forest/base-frontend --filter @forest/cxc-commerce-platform-web build
pnpm --dir /Users/lgd/project/forest/base-frontend build:cxc-platform-web
```

## 边界

- 平台端登录使用通用账号认证能力。
- 平台端权限、角色、菜单、数据范围后续由 RBAC / IAM 模块承载，不和账号凭证模型混在一起。
