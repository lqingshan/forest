UPDATE payment_order
SET channel = 'WECHAT_MINIAPP_PAYMENT'
WHERE channel = 'WECHAT_JSAPI';
