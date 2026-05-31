create table if not exists access_role (
    id bigserial primary key,
    role_code varchar(80) not null,
    role_name varchar(120) not null,
    boundary_type varchar(30) not null,
    boundary_id bigint not null,
    system_preset boolean not null default false,
    status varchar(20) not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null,
    constraint uk_access_role_boundary_code unique (boundary_type, boundary_id, role_code)
);

create index if not exists idx_access_role_boundary
    on access_role (boundary_type, boundary_id, deleted);

create table if not exists access_role_permission (
    id bigserial primary key,
    role_id bigint not null,
    permission_pattern varchar(160) not null,
    pattern_type varchar(20) not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null,
    constraint uk_access_role_permission_pattern unique (role_id, permission_pattern)
);

create index if not exists idx_access_role_permission_role
    on access_role_permission (role_id, deleted);

create table if not exists access_role_assignment (
    id bigserial primary key,
    subject_type varchar(30) not null,
    subject_id bigint not null,
    boundary_type varchar(30) not null,
    boundary_id bigint not null,
    role_id bigint not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null,
    constraint uk_access_role_assignment unique (subject_type, subject_id, boundary_type, boundary_id, role_id)
);

create index if not exists idx_access_role_assignment_subject
    on access_role_assignment (subject_type, subject_id, boundary_type, boundary_id, deleted);
