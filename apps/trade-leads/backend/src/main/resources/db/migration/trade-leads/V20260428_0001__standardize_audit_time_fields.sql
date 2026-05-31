alter table user_account
    add column if not exists modified_time timestamp;

update user_account
set modified_time = coalesce(modified_time, created_time, current_timestamp)
where modified_time is null;

alter table user_account
    alter column modified_time set not null;

alter table lead_unlock_record
    add column if not exists created_time timestamp;

alter table lead_unlock_record
    add column if not exists modified_time timestamp;

update lead_unlock_record
set created_time = coalesce(created_time, unlock_time, current_timestamp),
    modified_time = coalesce(modified_time, unlock_time, created_time, current_timestamp)
where created_time is null
   or modified_time is null;

alter table lead_unlock_record
    alter column created_time set not null;

alter table lead_unlock_record
    alter column modified_time set not null;

alter table point_log
    add column if not exists modified_time timestamp;

update point_log
set modified_time = coalesce(modified_time, created_time, current_timestamp)
where modified_time is null;

alter table point_log
    alter column modified_time set not null;

alter table recharge_order
    add column if not exists modified_time timestamp;

update recharge_order
set modified_time = coalesce(modified_time, paid_time, created_time, current_timestamp)
where modified_time is null;

alter table recharge_order
    alter column modified_time set not null;

alter table payment_order
    add column if not exists modified_time timestamp;

update payment_order
set modified_time = coalesce(modified_time, notify_time, paid_time, created_time, current_timestamp)
where modified_time is null;

alter table payment_order
    alter column modified_time set not null;

alter table point_balance
    add column if not exists created_time timestamp;

alter table point_balance
    add column if not exists modified_time timestamp;

do $$
begin
    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'point_balance'
          and column_name = 'updated_at'
    ) then
        update point_balance
        set created_time = coalesce(created_time, updated_at, current_timestamp),
            modified_time = coalesce(modified_time, updated_at, created_time, current_timestamp)
        where created_time is null
           or modified_time is null;
    else
        update point_balance
        set created_time = coalesce(created_time, current_timestamp),
            modified_time = coalesce(modified_time, created_time, current_timestamp)
        where created_time is null
           or modified_time is null;
    end if;
end $$;

alter table point_balance
    alter column created_time set not null;

alter table point_balance
    alter column modified_time set not null;

alter table point_balance
    drop column if exists updated_at;
