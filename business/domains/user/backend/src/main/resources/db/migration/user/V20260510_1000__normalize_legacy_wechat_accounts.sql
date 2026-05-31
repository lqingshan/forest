-- Normalize legacy trade-leads WeChat accounts created before the account type
-- was split into a scoped wechat_miniapp credential.
UPDATE account
SET type = 'wechat_miniapp',
    credential_scope = 'trade-leads-miniapp',
    modified_time = CURRENT_TIMESTAMP
WHERE type = 'wechat'
  AND credential_scope = 'GLOBAL';

UPDATE auth_session
SET account_type = 'wechat_miniapp',
    modified_time = CURRENT_TIMESTAMP
WHERE account_type = 'wechat';

UPDATE login_log
SET account_type = 'wechat_miniapp'
WHERE account_type = 'wechat';
