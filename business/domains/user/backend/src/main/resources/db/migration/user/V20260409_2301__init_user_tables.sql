create table if not exists app_user (
    id bigserial primary key,
    name varchar(100),
    avatar varchar(500),
    phone varchar(30),
    email varchar(120),
    status varchar(20) not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null
);

create table if not exists account (
    id bigserial primary key,
    type varchar(20) not null,
    identifier varchar(100) not null,
    secret varchar(255),
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null,
    constraint uk_account_type_identifier unique (type, identifier)
);

create table if not exists user_account (
    id bigserial primary key,
    user_id bigint not null,
    account_id bigint not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    constraint uk_user_account_user_account unique (user_id, account_id),
    constraint uk_user_account_account_id unique (account_id)
);

insert into app_user (name, status, deleted, created_time, modified_time)
select 'admin', 'ACTIVE', 0, current_timestamp, current_timestamp
where not exists (
    select 1
    from user_account ua
    join account a on a.id = ua.account_id
    where a.type = 'admin_password' and a.identifier = 'admin'
);

insert into account (type, identifier, secret, deleted, created_time, modified_time)
select 'admin_password', 'admin', '123456abc', 0, current_timestamp, current_timestamp
where not exists (
    select 1 from account where type = 'admin_password' and identifier = 'admin'
);

insert into user_account (user_id, account_id, deleted, created_time)
select
    (select max(id) from app_user where name = 'admin'),
    a.id,
    0,
    current_timestamp
from account a
where a.type = 'admin_password'
  and a.identifier = 'admin'
  and not exists (select 1 from user_account where account_id = a.id)
  and exists (select 1 from app_user where name = 'admin');
