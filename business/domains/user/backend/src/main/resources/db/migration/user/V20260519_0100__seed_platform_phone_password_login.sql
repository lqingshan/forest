-- Move the historical default platform account to the Web-PC phone/password model.
-- Do not edit historical migrations: old databases already recorded their checksums.

update account
set identifier = '+8618257147892',
    secret = null,
    status = 'ACTIVE',
    modified_time = current_timestamp
where type = 'platform_password'
  and credential_scope = 'GLOBAL'
  and identifier = 'admin'
  and not exists (
      select 1
      from account existing
      where existing.type = 'platform_password'
        and existing.credential_scope = 'GLOBAL'
        and existing.identifier = '+8618257147892'
  );

update account
set status = 'ACTIVE',
    modified_time = current_timestamp
where type = 'platform_password'
  and credential_scope = 'GLOBAL'
  and identifier = '+8618257147892';

with platform_user as (
    select ua.user_id
    from user_account ua
    join account a on a.id = ua.account_id
    where a.type = 'platform_password'
      and a.credential_scope = 'GLOBAL'
      and a.identifier = '+8618257147892'
    order by ua.user_id
    limit 1
)
update app_user
set phone = '+8618257147892',
    modified_time = current_timestamp
where id in (select user_id from platform_user);

insert into account (type, credential_scope, identifier, secret, status, deleted, created_time, modified_time)
select 'phone',
       'GLOBAL',
       '+8618257147892',
       null,
       'ACTIVE',
       0,
       current_timestamp,
       current_timestamp
where not exists (
    select 1
    from account
    where type = 'phone'
      and credential_scope = 'GLOBAL'
      and identifier = '+8618257147892'
);

insert into account (type, credential_scope, identifier, secret, status, deleted, created_time, modified_time)
select 'password',
       'GLOBAL',
       '+8618257147892',
       'sha256$931145d4ddd1811be545e4ac88a81f1fdbfaf0779c437efba16b884595274d11',
       'ACTIVE',
       0,
       current_timestamp,
       current_timestamp
where not exists (
    select 1
    from account
    where type = 'password'
      and credential_scope = 'GLOBAL'
      and identifier = '+8618257147892'
);

with platform_user as (
    select ua.user_id
    from user_account ua
    join account a on a.id = ua.account_id
    where a.type = 'platform_password'
      and a.credential_scope = 'GLOBAL'
      and a.identifier = '+8618257147892'
    order by ua.user_id
    limit 1
)
insert into user_account (user_id, account_id, deleted, created_time, modified_time)
select platform_user.user_id,
       account.id,
       0,
       current_timestamp,
       current_timestamp
from platform_user
join account on account.type = 'phone'
    and account.credential_scope = 'GLOBAL'
    and account.identifier = '+8618257147892'
where not exists (
    select 1
    from user_account
    where account_id = account.id
);

with platform_user as (
    select ua.user_id
    from user_account ua
    join account a on a.id = ua.account_id
    where a.type = 'platform_password'
      and a.credential_scope = 'GLOBAL'
      and a.identifier = '+8618257147892'
    order by ua.user_id
    limit 1
)
insert into user_account (user_id, account_id, deleted, created_time, modified_time)
select platform_user.user_id,
       account.id,
       0,
       current_timestamp,
       current_timestamp
from platform_user
join account on account.type = 'password'
    and account.credential_scope = 'GLOBAL'
    and account.identifier = '+8618257147892'
where not exists (
    select 1
    from user_account
    where account_id = account.id
);
