alter table recharge_order
    rename column points to credited_points;

alter table recharge_order
    rename column completed_time to paid_time;

alter table recharge_order
    add column if not exists paid_payment_order_id bigint;

update recharge_order
set status = 'PAID'
where status = 'COMPLETED';

update recharge_order
set status = 'CLOSED'
where status = 'CANCELLED';
