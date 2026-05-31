do $$
begin
    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'app_user'
          and column_name = 'nickname'
    ) and not exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'app_user'
          and column_name = 'name'
    ) then
        alter table app_user rename column nickname to name;
    end if;
end $$;
