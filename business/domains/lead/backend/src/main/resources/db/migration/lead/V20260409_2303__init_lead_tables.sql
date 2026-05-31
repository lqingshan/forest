create table if not exists lead (
    id bigserial primary key,
    source_type varchar(20),
    keywords varchar(200),
    name varchar(200) not null,
    category varchar(500),
    country varchar(50),
    phone varchar(50),
    email varchar(100),
    website varchar(200),
    intro text,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null
);

create table if not exists lead_unlock_record (
    id bigserial primary key,
    user_id bigint not null,
    lead_id bigint not null,
    point_cost integer not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    unlock_time timestamp not null,
    constraint uk_lead_unlock_user_lead unique (user_id, lead_id)
);
