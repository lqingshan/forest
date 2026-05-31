alter table auth_session
    add column if not exists created_id bigint;

alter table auth_session
    add column if not exists modified_id bigint;

alter table auth_session
    add column if not exists deleted integer default 0;

update auth_session
set deleted = 0
where deleted is null;

alter table auth_session
    alter column deleted set default 0;

alter table auth_session
    alter column deleted set not null;

alter table login_log
    add column if not exists created_id bigint;

alter table login_log
    add column if not exists modified_id bigint;

alter table login_log
    add column if not exists modified_time timestamp;

alter table login_log
    add column if not exists deleted integer default 0;

update login_log
set modified_time = created_time
where modified_time is null;

update login_log
set deleted = 0
where deleted is null;

alter table login_log
    alter column modified_time set not null;

alter table login_log
    alter column deleted set default 0;

alter table login_log
    alter column deleted set not null;

alter table sms_code
    add column if not exists created_id bigint;

alter table sms_code
    add column if not exists modified_id bigint;

alter table sms_code
    add column if not exists modified_time timestamp;

alter table sms_code
    add column if not exists deleted integer default 0;

update sms_code
set modified_time = created_time
where modified_time is null;

update sms_code
set deleted = 0
where deleted is null;

alter table sms_code
    alter column modified_time set not null;

alter table sms_code
    alter column deleted set default 0;

alter table sms_code
    alter column deleted set not null;
