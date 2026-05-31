create table if not exists point_balance (
    id bigserial primary key,
    user_id bigint not null,
    balance integer not null default 0,
    total_income integer not null default 0,
    total_spend integer not null default 0,
    version integer not null default 0,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    updated_at timestamp not null,
    constraint uk_point_balance_user_id unique (user_id)
);

create table if not exists point_log (
    id bigserial primary key,
    user_id bigint not null,
    direction varchar(20) not null,
    amount integer not null,
    balance_after integer not null,
    source_type varchar(20) not null,
    source_id bigint,
    biz_key varchar(120) not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    constraint uk_point_log_biz_key unique (biz_key)
);
