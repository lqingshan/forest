alter table account
    add column if not exists credential_scope varchar(80);

update account
set credential_scope = case
    when type in ('phone', 'admin_password') then 'GLOBAL'
    when type = 'wechat_miniapp' then 'trade-leads-miniapp'
    else 'GLOBAL'
end
where credential_scope is null
   or credential_scope = '';

alter table account
    alter column credential_scope set not null;

alter table account
    drop constraint if exists uk_account_type_identifier;

alter table account
    add constraint uk_account_type_scope_identifier unique (type, credential_scope, identifier);
