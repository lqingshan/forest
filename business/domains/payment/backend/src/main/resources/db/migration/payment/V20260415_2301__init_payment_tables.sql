create table if not exists payment_order (
    id bigserial primary key,
    payment_no varchar(64) not null,
    biz_type varchar(32) not null,
    biz_order_id bigint not null,
    channel varchar(32) not null,
    amount_cents integer not null,
    status varchar(32) not null,
    out_trade_no varchar(64) not null,
    prepay_id varchar(128),
    transaction_id varchar(64),
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    notify_time timestamp,
    paid_time timestamp,
    constraint uk_payment_order_payment_no unique (payment_no),
    constraint uk_payment_order_out_trade_no unique (out_trade_no)
);

create index if not exists idx_payment_order_biz_ref
    on payment_order (biz_type, biz_order_id);

create index if not exists idx_payment_order_status
    on payment_order (status);
