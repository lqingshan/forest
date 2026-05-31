create table if not exists organization (
    id bigserial primary key,
    organization_no varchar(64) not null,
    organization_name varchar(150) not null,
    status varchar(20) not null,
    certification_status varchar(20) not null,
    current_certification_id bigint,
    owner_user_id bigint not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null,
    constraint uk_organization_no unique (organization_no)
);

create index if not exists idx_organization_owner_user
    on organization (owner_user_id);

create index if not exists idx_organization_status
    on organization (status, certification_status);

create table if not exists organization_department (
    id bigserial primary key,
    department_no varchar(64) not null,
    organization_id bigint not null,
    parent_id bigint,
    department_name varchar(120) not null,
    default_department boolean not null default false,
    sort_order integer not null default 0,
    status varchar(20) not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null,
    constraint uk_organization_department_no unique (department_no)
);

create index if not exists idx_organization_department_org
    on organization_department (organization_id, parent_id, deleted);

create index if not exists idx_organization_department_default
    on organization_department (organization_id, default_department, deleted);

create table if not exists organization_member (
    id bigserial primary key,
    member_no varchar(64) not null,
    organization_id bigint not null,
    user_id bigint not null,
    department_id bigint not null,
    role varchar(20) not null,
    status varchar(20) not null,
    joined_time timestamp not null,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null,
    constraint uk_organization_member_no unique (member_no),
    constraint uk_organization_member_org_user unique (organization_id, user_id)
);

create index if not exists idx_organization_member_org
    on organization_member (organization_id, status, role);

create index if not exists idx_organization_member_user
    on organization_member (user_id, status);

create index if not exists idx_organization_member_department
    on organization_member (department_id, status);

create table if not exists organization_certification (
    id bigserial primary key,
    certification_no varchar(64) not null,
    organization_id bigint not null,
    company_name varchar(150) not null,
    unified_social_credit_code varchar(64) not null,
    legal_representative_name varchar(100) not null,
    business_license_file_no varchar(64) not null,
    contact_name varchar(100) not null,
    contact_phone varchar(30) not null,
    status varchar(20) not null,
    submitted_by_user_id bigint not null,
    reviewed_by_user_id bigint,
    reviewed_time timestamp,
    review_remark varchar(500),
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null,
    constraint uk_organization_certification_no unique (certification_no)
);

create index if not exists idx_organization_certification_org
    on organization_certification (organization_id, created_time);

create index if not exists idx_organization_certification_status
    on organization_certification (status, created_time);
