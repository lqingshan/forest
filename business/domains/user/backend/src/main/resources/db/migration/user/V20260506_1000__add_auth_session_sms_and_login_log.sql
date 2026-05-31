alter table account
    add column if not exists status varchar(20) not null default 'ACTIVE';

update account
set secret = 'sha256$931145d4ddd1811be545e4ac88a81f1fdbfaf0779c437efba16b884595274d11'
where type = 'admin_password'
  and identifier = 'admin'
  and secret = '123456abc';

create table if not exists sms_code (
    id bigserial primary key,
    phone varchar(30) not null,
    scene varchar(30) not null,
    code_hash varchar(255) not null,
    verified boolean not null default false,
    attempt_count integer not null default 0,
    send_ip varchar(64),
    expires_at timestamp not null,
    created_time timestamp not null
);

create index if not exists idx_sms_code_phone_scene_expires
    on sms_code (phone, scene, expires_at);

create table if not exists auth_session (
    id bigserial primary key,
    user_id bigint not null,
    account_id bigint not null,
    account_type varchar(30) not null,
    client_type varchar(30) not null,
    app_code varchar(60) not null,
    refresh_token_jti varchar(80) not null,
    status varchar(20) not null,
    login_ip varchar(64),
    user_agent varchar(500),
    last_active_time timestamp,
    refresh_expires_at timestamp not null,
    created_time timestamp not null,
    modified_time timestamp not null
);

create index if not exists idx_auth_session_user_status
    on auth_session (user_id, status);

create index if not exists idx_auth_session_refresh_jti
    on auth_session (refresh_token_jti);

create table if not exists login_log (
    id bigserial primary key,
    user_id bigint,
    account_id bigint,
    session_id bigint,
    account_type varchar(30),
    identifier_snapshot varchar(120),
    phone_snapshot varchar(30),
    client_type varchar(30),
    app_code varchar(60),
    result varchar(20) not null,
    failure_reason varchar(255),
    verification_mode varchar(30),
    login_ip varchar(64),
    user_agent varchar(500),
    created_time timestamp not null
);

create index if not exists idx_login_log_user_created
    on login_log (user_id, created_time);

create index if not exists idx_login_log_phone_created
    on login_log (phone_snapshot, created_time);
