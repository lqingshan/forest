create table if not exists recharge_order (
    id bigserial primary key,
    user_id bigint not null,
    package_code varchar(40) not null,
    amount_cents integer not null,
    points integer not null,
    status varchar(20) not null,
    recharge_no varchar(64) not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    completed_time timestamp,
    constraint uk_recharge_order_recharge_no unique (recharge_no)
);
