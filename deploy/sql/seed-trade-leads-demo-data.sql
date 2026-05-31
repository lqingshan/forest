begin;

insert into app_user (
    id, name, avatar, phone, email, status, created_id, modified_id, deleted, created_time, modified_time
) values
    (1001, 'Alice Zhang', 'https://i.pravatar.cc/200?img=1', '13800001001', 'alice@demo.local', 'ACTIVE', 1, 1, 0, '2026-04-20 09:00:00', '2026-04-22 10:00:00'),
    (1002, 'Bob Chen', 'https://i.pravatar.cc/200?img=2', '13800001002', 'bob@demo.local', 'ACTIVE', 1, 1, 0, '2026-04-20 09:05:00', '2026-04-22 10:05:00'),
    (1003, 'Cathy Liu', 'https://i.pravatar.cc/200?img=3', '13800001003', 'cathy@demo.local', 'FROZEN', 1, 1, 0, '2026-04-20 09:10:00', '2026-04-22 10:10:00'),
    (1004, 'David Wang', 'https://i.pravatar.cc/200?img=4', '13800001004', 'david@demo.local', 'ACTIVE', 1, 1, 0, '2026-04-20 09:15:00', '2026-04-22 10:15:00'),
    (1005, 'Eva Sun', 'https://i.pravatar.cc/200?img=5', '13800001005', 'eva@demo.local', 'ACTIVE', 1, 1, 0, '2026-04-20 09:20:00', '2026-04-22 10:20:00'),
    (1006, 'Frank Zhou', 'https://i.pravatar.cc/200?img=6', '13800001006', 'frank@demo.local', 'DISABLED', 1, 1, 0, '2026-04-20 09:25:00', '2026-04-22 10:25:00'),
    (1007, 'Grace Lin', 'https://i.pravatar.cc/200?img=7', '13800001007', 'grace@demo.local', 'ACTIVE', 1, 1, 0, '2026-04-20 09:30:00', '2026-04-22 10:30:00'),
    (1008, 'Henry Xu', 'https://i.pravatar.cc/200?img=8', '13800001008', 'henry@demo.local', 'ACTIVE', 1, 1, 0, '2026-04-20 09:35:00', '2026-04-22 10:35:00'),
    (1009, 'Iris Gao', 'https://i.pravatar.cc/200?img=9', '13800001009', 'iris@demo.local', 'ACTIVE', 1, 1, 0, '2026-04-20 09:40:00', '2026-04-22 10:40:00'),
    (1010, 'Jack Hu', 'https://i.pravatar.cc/200?img=10', '13800001010', 'jack@demo.local', 'ACTIVE', 1, 1, 0, '2026-04-20 09:45:00', '2026-04-22 10:45:00'),
    (1011, 'Kelly He', 'https://i.pravatar.cc/200?img=11', '13800001011', 'kelly@demo.local', 'ACTIVE', 1, 1, 0, '2026-04-20 09:50:00', '2026-04-22 10:50:00')
on conflict (id) do nothing;

insert into account (
    id, type, identifier, secret, created_id, modified_id, deleted, created_time, modified_time
) values
    (1001, 'wechat', 'mock-openid-user-1001', null, 1, 1, 0, '2026-04-20 09:00:30', '2026-04-20 09:00:30'),
    (1002, 'wechat', 'mock-openid-user-1002', null, 1, 1, 0, '2026-04-20 09:05:30', '2026-04-20 09:05:30'),
    (1003, 'wechat', 'mock-openid-user-1003', null, 1, 1, 0, '2026-04-20 09:10:30', '2026-04-20 09:10:30'),
    (1004, 'wechat', 'mock-openid-user-1004', null, 1, 1, 0, '2026-04-20 09:15:30', '2026-04-20 09:15:30'),
    (1005, 'wechat', 'mock-openid-user-1005', null, 1, 1, 0, '2026-04-20 09:20:30', '2026-04-20 09:20:30'),
    (1006, 'wechat', 'mock-openid-user-1006', null, 1, 1, 0, '2026-04-20 09:25:30', '2026-04-20 09:25:30'),
    (1007, 'wechat', 'mock-openid-user-1007', null, 1, 1, 0, '2026-04-20 09:30:30', '2026-04-20 09:30:30'),
    (1008, 'wechat', 'mock-openid-user-1008', null, 1, 1, 0, '2026-04-20 09:35:30', '2026-04-20 09:35:30'),
    (1009, 'wechat', 'mock-openid-user-1009', null, 1, 1, 0, '2026-04-20 09:40:30', '2026-04-20 09:40:30'),
    (1010, 'wechat', 'mock-openid-user-1010', null, 1, 1, 0, '2026-04-20 09:45:30', '2026-04-20 09:45:30'),
    (1011, 'wechat', 'mock-openid-user-1011', null, 1, 1, 0, '2026-04-20 09:50:30', '2026-04-20 09:50:30')
on conflict (id) do nothing;

insert into user_account (
    id, user_id, account_id, created_id, modified_id, deleted, created_time, modified_time
) values
    (1001, 1001, 1001, 1, 1, 0, '2026-04-20 09:01:00', '2026-04-20 09:01:00'),
    (1002, 1002, 1002, 1, 1, 0, '2026-04-20 09:06:00', '2026-04-20 09:06:00'),
    (1003, 1003, 1003, 1, 1, 0, '2026-04-20 09:11:00', '2026-04-20 09:11:00'),
    (1004, 1004, 1004, 1, 1, 0, '2026-04-20 09:16:00', '2026-04-20 09:16:00'),
    (1005, 1005, 1005, 1, 1, 0, '2026-04-20 09:21:00', '2026-04-20 09:21:00'),
    (1006, 1006, 1006, 1, 1, 0, '2026-04-20 09:26:00', '2026-04-20 09:26:00'),
    (1007, 1007, 1007, 1, 1, 0, '2026-04-20 09:31:00', '2026-04-20 09:31:00'),
    (1008, 1008, 1008, 1, 1, 0, '2026-04-20 09:36:00', '2026-04-20 09:36:00'),
    (1009, 1009, 1009, 1, 1, 0, '2026-04-20 09:41:00', '2026-04-20 09:41:00'),
    (1010, 1010, 1010, 1, 1, 0, '2026-04-20 09:46:00', '2026-04-20 09:46:00'),
    (1011, 1011, 1011, 1, 1, 0, '2026-04-20 09:51:00', '2026-04-20 09:51:00')
on conflict (id) do nothing;

insert into lead (
    id, source_type, keywords, name, category, country, phone, email, website, intro,
    created_id, modified_id, deleted, created_time, modified_time
) values
    (2001, 'MANUAL', 'AI,SaaS,客服', 'Shenzhen Aurora AI', 'AI SaaS', 'China', '+86-755-1001', 'hello@aurora-ai.cn', 'https://aurora-ai.cn', '提供智能客服与销售自动化服务。', 1, 1, 0, '2026-04-18 09:00:00', '2026-04-18 09:00:00'),
    (2002, 'IMPORT', 'CRM,私域,营销', 'Hangzhou Private Growth', 'Marketing CRM', 'China', '+86-571-1002', 'contact@privategrowth.cn', 'https://privategrowth.cn', '专注私域增长和客户运营工具。', 1, 1, 0, '2026-04-18 09:10:00', '2026-04-18 09:10:00'),
    (2003, 'MANUAL', '支付,跨境,电商', 'Guangzhou Border Pay', 'Fintech', 'China', '+86-20-1003', 'biz@borderpay.cn', 'https://borderpay.cn', '提供跨境支付与结算方案。', 1, 1, 0, '2026-04-18 09:20:00', '2026-04-18 09:20:00'),
    (2004, 'IMPORT', 'solar,energy,b2b', 'California Solar Labs', 'New Energy', 'USA', '+1-415-1004', 'sales@csolarlabs.com', 'https://csolarlabs.com', '面向企业客户的新能源设备供应商。', 1, 1, 0, '2026-04-18 09:30:00', '2026-04-18 09:30:00'),
    (2005, 'MANUAL', 'medical,healthcare,clinic', 'Boston MediFlow', 'Healthcare SaaS', 'USA', '+1-617-1005', 'contact@mediflow.com', 'https://mediflow.com', '为诊所提供预约和患者管理系统。', 1, 1, 0, '2026-04-18 09:40:00', '2026-04-18 09:40:00'),
    (2006, 'IMPORT', 'retail,pos,inventory', 'Seattle Store Pulse', 'Retail Tech', 'USA', '+1-206-1006', 'team@storepulse.com', 'https://storepulse.com', '零售门店 POS 与库存 SaaS。', 1, 1, 0, '2026-04-18 09:50:00', '2026-04-18 09:50:00'),
    (2007, 'MANUAL', 'logistics,tracking,shipment', 'Singapore Ship Matrix', 'Logistics', 'Singapore', '+65-1007', 'ops@shipmatrix.sg', 'https://shipmatrix.sg', '提供东南亚区域物流追踪平台。', 1, 1, 0, '2026-04-18 10:00:00', '2026-04-18 10:00:00'),
    (2008, 'IMPORT', 'education,saas,school', 'London Edu Orbit', 'Education SaaS', 'UK', '+44-20-1008', 'hello@eduorbit.co.uk', 'https://eduorbit.co.uk', '学校教务与家校协同平台。', 1, 1, 0, '2026-04-18 10:10:00', '2026-04-18 10:10:00'),
    (2009, 'MANUAL', 'factory,erp,manufacturing', 'Berlin Factory Grid', 'Industrial SaaS', 'Germany', '+49-30-1009', 'sales@factorygrid.de', 'https://factorygrid.de', '制造业 ERP 与排产可视化平台。', 1, 1, 0, '2026-04-18 10:20:00', '2026-04-18 10:20:00'),
    (2010, 'IMPORT', 'coffee,franchise,crm', 'Melbourne Coffee Chain', 'Food Franchise', 'Australia', '+61-3-1010', 'grow@coffeechain.au', 'https://coffeechain.au', '咖啡连锁会员与门店经营系统。', 1, 1, 0, '2026-04-18 10:30:00', '2026-04-18 10:30:00'),
    (2011, 'MANUAL', 'beauty,salon,membership', 'Tokyo Salon Works', 'Beauty SaaS', 'Japan', '+81-3-1011', 'support@salonworks.jp', 'https://salonworks.jp', '沙龙门店预约与会员系统。', 1, 1, 0, '2026-04-18 10:40:00', '2026-04-18 10:40:00'),
    (2012, 'IMPORT', 'hotel,booking,travel', 'Dubai Stay Engine', 'Travel Tech', 'UAE', '+971-4-1012', 'bd@stayengine.ae', 'https://stayengine.ae', '酒店预订和渠道管理服务。', 1, 1, 0, '2026-04-18 10:50:00', '2026-04-18 10:50:00'),
    (2013, 'MANUAL', 'insurance,broker,crm', 'Toronto Policy Desk', 'Insurance SaaS', 'Canada', '+1-416-1013', 'team@policydesk.ca', 'https://policydesk.ca', '保险经纪团队客户管理平台。', 1, 1, 0, '2026-04-18 11:00:00', '2026-04-18 11:00:00'),
    (2014, 'IMPORT', 'construction,bim,project', 'Oslo Build Scope', 'Construction Tech', 'Norway', '+47-21-1014', 'info@buildscope.no', 'https://buildscope.no', '建筑项目协同和现场数字化工具。', 1, 1, 0, '2026-04-18 11:10:00', '2026-04-18 11:10:00'),
    (2015, 'MANUAL', 'hr,recruiting,talent', 'Dublin Talent Dock', 'HR Tech', 'Ireland', '+353-1-1015', 'contact@talentdock.ie', 'https://talentdock.ie', '招聘流程和人才库管理服务。', 1, 1, 0, '2026-04-18 11:20:00', '2026-04-18 11:20:00'),
    (2016, 'IMPORT', 'finance,billing,subscription', 'Paris Billing Cloud', 'Billing SaaS', 'France', '+33-1-1016', 'sales@billingcloud.fr', 'https://billingcloud.fr', '订阅计费与财务对账平台。', 1, 1, 0, '2026-04-18 11:30:00', '2026-04-18 11:30:00'),
    (2017, 'MANUAL', 'warehouse,iot,robot', 'Seoul Robo Warehouse', 'Smart Warehouse', 'Korea', '+82-2-1017', 'robot@warehouse.kr', 'https://warehouse.kr', '仓储机器人与 IoT 调度平台。', 1, 1, 0, '2026-04-18 11:40:00', '2026-04-18 11:40:00'),
    (2018, 'IMPORT', 'legal,contract,workflow', 'Madrid Legal Flow', 'Legal Tech', 'Spain', '+34-91-1018', 'hola@legalflow.es', 'https://legalflow.es', '合同审批与法务工作流管理。', 1, 1, 0, '2026-04-18 11:50:00', '2026-04-18 11:50:00')
on conflict (id) do nothing;

insert into recharge_order (
    id, user_id, package_code, amount_cents, credited_points, status, recharge_no,
    created_id, modified_id, deleted, created_time, modified_time, paid_time, paid_payment_order_id
) values
    (5001, 1001, 'starter', 990, 99, 'PAID', 'RECHARGE-20260420091000-10010001', 1001, 1001, 0, '2026-04-20 09:10:00', '2026-04-20 09:11:12', '2026-04-20 09:11:12', 6001),
    (5002, 1002, 'growth', 2990, 299, 'PAID', 'RECHARGE-20260420101500-10020001', 1002, 1002, 0, '2026-04-20 10:15:00', '2026-04-20 10:16:33', '2026-04-20 10:16:33', 6002),
    (5003, 1004, 'pro', 4990, 499, 'PAID', 'RECHARGE-20260420112000-10040001', 1004, 1004, 0, '2026-04-20 11:20:00', '2026-04-20 11:22:18', '2026-04-20 11:22:18', 6003),
    (5004, 1005, 'starter', 990, 99, 'CREATED', 'RECHARGE-20260421103000-10050001', 1005, 1005, 0, '2026-04-21 10:30:00', '2026-04-21 10:30:00', null, null),
    (5005, 1006, 'growth', 2990, 299, 'CLOSED', 'RECHARGE-20260421111500-10060001', 1006, 1006, 0, '2026-04-21 11:15:00', '2026-04-21 11:15:00', null, null),
    (5006, 1007, 'starter', 990, 99, 'PAID', 'RECHARGE-20260422091500-10070001', 1007, 1007, 0, '2026-04-22 09:15:00', '2026-04-22 09:16:04', '2026-04-22 09:16:04', 6006),
    (5007, 1008, 'growth', 2990, 299, 'PAID', 'RECHARGE-20260422103000-10080001', 1008, 1008, 0, '2026-04-22 10:30:00', '2026-04-22 10:31:44', '2026-04-22 10:31:44', 6007)
on conflict (id) do nothing;

insert into payment_order (
    id, payment_no, biz_type, biz_order_id, channel, amount_cents, status, out_trade_no,
    prepay_id, transaction_id, created_id, modified_id, deleted, created_time, modified_time, notify_time, paid_time
) values
    (6001, 'PAY-20260420091000-5001', 'RECHARGE', 5001, 'WECHAT_MINIAPP_PAYMENT', 990, 'SUCCESS', 'OTN-20260420-5001-01', 'wx_prepay_5001_success', 'wx_txn_5001_success', 1001, 1001, 0, '2026-04-20 09:10:05', '2026-04-20 09:11:10', '2026-04-20 09:11:10', '2026-04-20 09:11:12'),
    (6002, 'PAY-20260420101500-5002', 'RECHARGE', 5002, 'WECHAT_MINIAPP_PAYMENT', 2990, 'SUCCESS', 'OTN-20260420-5002-01', 'wx_prepay_5002_success', 'wx_txn_5002_success', 1002, 1002, 0, '2026-04-20 10:15:10', '2026-04-20 10:16:30', '2026-04-20 10:16:30', '2026-04-20 10:16:33'),
    (6003, 'PAY-20260420112000-5003', 'RECHARGE', 5003, 'WECHAT_MINIAPP_PAYMENT', 4990, 'SUCCESS', 'OTN-20260420-5003-01', 'wx_prepay_5003_success', 'wx_txn_5003_success', 1004, 1004, 0, '2026-04-20 11:20:08', '2026-04-20 11:22:10', '2026-04-20 11:22:10', '2026-04-20 11:22:18'),
    (6004, 'PAY-20260421103000-5004', 'RECHARGE', 5004, 'WECHAT_MINIAPP_PAYMENT', 990, 'PREPAY_CREATED', 'OTN-20260421-5004-01', 'wx_prepay_5004_waiting', null, 1005, 1005, 0, '2026-04-21 10:30:06', '2026-04-21 10:30:06', null, null),
    (6005, 'PAY-20260421111500-5005', 'RECHARGE', 5005, 'WECHAT_MINIAPP_PAYMENT', 2990, 'CLOSED', 'OTN-20260421-5005-01', 'wx_prepay_5005_closed', null, 1006, 1006, 0, '2026-04-21 11:15:07', '2026-04-21 11:15:07', null, null),
    (6006, 'PAY-20260422091500-5006', 'RECHARGE', 5006, 'WECHAT_MINIAPP_PAYMENT', 990, 'SUCCESS', 'OTN-20260422-5006-01', 'wx_prepay_5006_success', 'wx_txn_5006_success', 1007, 1007, 0, '2026-04-22 09:15:06', '2026-04-22 09:16:01', '2026-04-22 09:16:01', '2026-04-22 09:16:04'),
    (6007, 'PAY-20260422103000-5007', 'RECHARGE', 5007, 'WECHAT_MINIAPP_PAYMENT', 2990, 'SUCCESS', 'OTN-20260422-5007-01', 'wx_prepay_5007_success', 'wx_txn_5007_success', 1008, 1008, 0, '2026-04-22 10:30:05', '2026-04-22 10:31:40', '2026-04-22 10:31:40', '2026-04-22 10:31:44'),
    (6008, 'PAY-20260420090830-5001', 'RECHARGE', 5001, 'WECHAT_MINIAPP_PAYMENT', 990, 'CLOSED', 'OTN-20260420-5001-00', 'wx_prepay_5001_closed', null, 1001, 1001, 0, '2026-04-20 09:08:30', '2026-04-20 09:08:30', null, null),
    (6009, 'PAY-20260420111830-5003', 'RECHARGE', 5003, 'WECHAT_MINIAPP_PAYMENT', 4990, 'CLOSED', 'OTN-20260420-5003-00', 'wx_prepay_5003_closed', null, 1004, 1004, 0, '2026-04-20 11:18:30', '2026-04-20 11:18:30', null, null)
on conflict (id) do nothing;

insert into lead_unlock_record (
    id, user_id, lead_id, point_cost, created_id, modified_id, deleted, created_time, modified_time, unlock_time
) values
    (7001, 1001, 2001, 5, 1001, 1001, 0, '2026-04-20 09:20:00', '2026-04-20 09:20:00', '2026-04-20 09:20:00'),
    (7002, 1001, 2002, 5, 1001, 1001, 0, '2026-04-20 09:35:00', '2026-04-20 09:35:00', '2026-04-20 09:35:00'),
    (7003, 1002, 2003, 5, 1002, 1002, 0, '2026-04-20 10:25:00', '2026-04-20 10:25:00', '2026-04-20 10:25:00'),
    (7004, 1004, 2004, 5, 1004, 1004, 0, '2026-04-20 11:40:00', '2026-04-20 11:40:00', '2026-04-20 11:40:00'),
    (7005, 1004, 2005, 5, 1004, 1004, 0, '2026-04-20 11:45:00', '2026-04-20 11:45:00', '2026-04-20 11:45:00'),
    (7006, 1004, 2006, 5, 1004, 1004, 0, '2026-04-20 11:50:00', '2026-04-20 11:50:00', '2026-04-20 11:50:00'),
    (7007, 1007, 2007, 5, 1007, 1007, 0, '2026-04-22 09:25:00', '2026-04-22 09:25:00', '2026-04-22 09:25:00')
on conflict (id) do nothing;

insert into point_log (
    id, user_id, direction, amount, balance_after, source_type, source_id, biz_key,
    created_id, modified_id, deleted, created_time, modified_time
) values
    (8001, 1001, 'INCOME', 99, 99, 'RECHARGE', 5001, 'recharge:5001', 1001, 1001, 0, '2026-04-20 09:11:13', '2026-04-20 09:11:13'),
    (8002, 1001, 'SPEND', 5, 94, 'UNLOCK', 7001, 'unlock:1001:2001', 1001, 1001, 0, '2026-04-20 09:20:01', '2026-04-20 09:20:01'),
    (8003, 1001, 'SPEND', 5, 89, 'UNLOCK', 7002, 'unlock:1001:2002', 1001, 1001, 0, '2026-04-20 09:35:01', '2026-04-20 09:35:01'),
    (8004, 1002, 'INCOME', 299, 299, 'RECHARGE', 5002, 'recharge:5002', 1002, 1002, 0, '2026-04-20 10:16:34', '2026-04-20 10:16:34'),
    (8005, 1002, 'SPEND', 5, 294, 'UNLOCK', 7003, 'unlock:1002:2003', 1002, 1002, 0, '2026-04-20 10:25:01', '2026-04-20 10:25:01'),
    (8006, 1004, 'INCOME', 499, 499, 'RECHARGE', 5003, 'recharge:5003', 1004, 1004, 0, '2026-04-20 11:22:19', '2026-04-20 11:22:19'),
    (8007, 1004, 'SPEND', 5, 494, 'UNLOCK', 7004, 'unlock:1004:2004', 1004, 1004, 0, '2026-04-20 11:40:01', '2026-04-20 11:40:01'),
    (8008, 1004, 'SPEND', 5, 489, 'UNLOCK', 7005, 'unlock:1004:2005', 1004, 1004, 0, '2026-04-20 11:45:01', '2026-04-20 11:45:01'),
    (8009, 1004, 'SPEND', 5, 484, 'UNLOCK', 7006, 'unlock:1004:2006', 1004, 1004, 0, '2026-04-20 11:50:01', '2026-04-20 11:50:01'),
    (8010, 1007, 'INCOME', 99, 99, 'RECHARGE', 5006, 'recharge:5006', 1007, 1007, 0, '2026-04-22 09:16:05', '2026-04-22 09:16:05'),
    (8011, 1007, 'SPEND', 5, 94, 'UNLOCK', 7007, 'unlock:1007:2007', 1007, 1007, 0, '2026-04-22 09:25:01', '2026-04-22 09:25:01'),
    (8012, 1008, 'INCOME', 299, 299, 'RECHARGE', 5007, 'recharge:5007', 1008, 1008, 0, '2026-04-22 10:31:45', '2026-04-22 10:31:45')
on conflict (id) do nothing;

insert into point_balance (
    id, user_id, balance, total_income, total_spend, version, created_id, modified_id, deleted, created_time, modified_time
) values
    (9001, 1001, 89, 99, 10, 3, 1001, 1001, 0, '2026-04-20 09:00:00', '2026-04-20 09:35:01'),
    (9002, 1002, 294, 299, 5, 2, 1002, 1002, 0, '2026-04-20 09:05:00', '2026-04-20 10:25:01'),
    (9003, 1003, 0, 0, 0, 0, 1003, 1003, 0, '2026-04-20 09:10:00', '2026-04-22 10:10:00'),
    (9004, 1004, 484, 499, 15, 4, 1004, 1004, 0, '2026-04-20 09:15:00', '2026-04-20 11:50:01'),
    (9005, 1005, 0, 0, 0, 0, 1005, 1005, 0, '2026-04-20 09:20:00', '2026-04-21 10:30:00'),
    (9006, 1006, 0, 0, 0, 0, 1006, 1006, 0, '2026-04-20 09:25:00', '2026-04-21 11:15:00'),
    (9007, 1007, 94, 99, 5, 2, 1007, 1007, 0, '2026-04-20 09:30:00', '2026-04-22 09:25:01'),
    (9008, 1008, 299, 299, 0, 1, 1008, 1008, 0, '2026-04-20 09:35:00', '2026-04-22 10:31:45'),
    (9009, 1009, 0, 0, 0, 0, 1009, 1009, 0, '2026-04-20 09:40:00', '2026-04-22 10:40:00'),
    (9010, 1010, 0, 0, 0, 0, 1010, 1010, 0, '2026-04-20 09:45:00', '2026-04-22 10:45:00'),
    (9011, 1011, 0, 0, 0, 0, 1011, 1011, 0, '2026-04-20 09:50:00', '2026-04-22 10:50:00')
on conflict (id) do nothing;

select setval(pg_get_serial_sequence('app_user', 'id'), coalesce((select max(id) from app_user), 1), true);
select setval(pg_get_serial_sequence('account', 'id'), coalesce((select max(id) from account), 1), true);
select setval(pg_get_serial_sequence('user_account', 'id'), coalesce((select max(id) from user_account), 1), true);
select setval(pg_get_serial_sequence('lead', 'id'), coalesce((select max(id) from lead), 1), true);
select setval(pg_get_serial_sequence('lead_unlock_record', 'id'), coalesce((select max(id) from lead_unlock_record), 1), true);
select setval(pg_get_serial_sequence('point_balance', 'id'), coalesce((select max(id) from point_balance), 1), true);
select setval(pg_get_serial_sequence('point_log', 'id'), coalesce((select max(id) from point_log), 1), true);
select setval(pg_get_serial_sequence('recharge_order', 'id'), coalesce((select max(id) from recharge_order), 1), true);
select setval(pg_get_serial_sequence('payment_order', 'id'), coalesce((select max(id) from payment_order), 1), true);

commit;
