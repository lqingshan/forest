# 微信小程序支付链路测试清单

## 目标

- 使用微信小程序体验版真实跑通 `starter / 新客包 / 0.01 元 / 99 积分` 充值支付链路。
- 最终以服务端状态为准：`payment_order.status=SUCCESS`、`recharge_order.status=PAID`、用户积分增加 `99`。
- 微信支付回调地址固定为 `https://leads.haitunai.cn/api/open/wechat/pay/notify`。

## 发布前检查

- 后端生产环境开启真实支付：`WECHAT_PAY_ENABLED=true`、`WECHAT_PAY_MOCK_ENABLED=false`。
- 后端生产环境 `WECHAT_PAY_NOTIFY_URL=https://leads.haitunai.cn/api/open/wechat/pay/notify`。
- `starter` 套餐为 `amountCents=1`、`creditedPoints=99`。
- 小程序体验版使用 prod 包：`build:prod` 构建产物中包含 `https://leads.haitunai.cn`，不包含 `https://localleads.haitunai.cn`。
- 微信公众平台已把 `https://leads.haitunai.cn` 配为 request 合法域名。
- 微信开发者工具上传体验版时使用真实小程序 AppID，不使用 `touristappid`。

## 执行步骤

1. 部署包含 0.01 元套餐和 prod notify URL 的后端版本。
2. 执行小程序 prod 构建。
3. 在微信开发者工具打开 `/Users/lgd/project/forest/apps/trade-leads/clients/client-wechat-miniapp` 并上传体验版。
4. 真机打开体验版，登录后进入充值页。
5. 选择 `starter`，确认收银台金额是 `0.01 元`。
6. 完成支付后观察结果页，允许短暂显示 `processing`，最终应变成成功。
7. 按下面 SQL 核对支付单、充值单和积分流水。
8. 额外执行一次取消支付，确认不会入账。

## 验收 SQL

```sql
select id, payment_no, biz_type, biz_order_id, out_trade_no, prepay_id,
       transaction_id, amount_cents, status, notify_time, paid_time
from payment_order
order by id desc
limit 5;

select id, user_id, package_code, amount_cents, credited_points,
       status, paid_payment_order_id, paid_time
from recharge_order
order by id desc
limit 5;

select user_id, balance, total_income, total_spend, modified_time
from point_balance
where user_id = <测试用户ID>;

select user_id, direction, amount, balance_after, source_type,
       source_id, biz_key, created_time
from point_log
where user_id = <测试用户ID>
order by id desc
limit 5;
```

## 失败定位口径

- 小程序无法请求接口：优先检查体验版是否用 `build:prod` 构建，以及微信公众平台 request 合法域名。
- 无法拉起支付：检查 `payment_order.prepay_id` 是否生成、微信支付证书和商户配置是否正确。
- 支付成功但结果页一直处理中：检查微信是否回调 `notify_url`，以及后端是否验签失败。
- 支付成功但积分未增加：检查 `PaymentSucceededEvent -> RechargePaidEvent -> point_log` 事件链路。
- 取消支付后入账：这是严重错误，应检查是否错误调用 mock notify 或重复处理非成功状态。
