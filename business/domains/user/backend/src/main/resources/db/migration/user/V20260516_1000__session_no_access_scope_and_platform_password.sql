alter table auth_session add column if not exists session_no varchar(64);
update auth_session set session_no = 'AS' || id where session_no is null;
alter table auth_session alter column session_no set not null;
alter table auth_session drop constraint if exists uk_auth_session_session_no;
alter table auth_session add constraint uk_auth_session_session_no unique (session_no);

alter table auth_session add column if not exists access_scope varchar(20);
update auth_session
set access_scope = case
    when client_type = 'WECHAT_MINIAPP' then 'CLIENT'
    else 'PLATFORM'
end
where access_scope is null;
alter table auth_session alter column access_scope set not null;

update account
set type = 'platform_password'
where type = 'admin_password';
