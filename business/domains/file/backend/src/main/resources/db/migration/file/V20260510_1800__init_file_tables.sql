create table if not exists file_object (
    id bigserial primary key,
    file_no varchar(64) not null,
    business_app_code varchar(80) not null,
    uploaded_client_app_code varchar(80) not null,
    uploader_user_id bigint not null,
    uploader_account_id bigint not null,
    bucket varchar(128) not null,
    object_key varchar(512) not null,
    etag varchar(128),
    original_name varchar(255) not null,
    extension varchar(32),
    content_type varchar(120) not null,
    file_category varchar(20) not null,
    size_bytes bigint not null,
    sha256 varchar(64),
    image_width integer,
    image_height integer,
    status varchar(20) not null,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null,
    deleted_time timestamp,
    constraint uk_file_object_file_no unique (file_no),
    constraint uk_file_object_object_key unique (bucket, object_key)
);

create index if not exists idx_file_object_uploader
    on file_object (business_app_code, uploader_user_id, status);

create index if not exists idx_file_object_created_time
    on file_object (created_time);

create table if not exists file_upload_session (
    id bigserial primary key,
    upload_session_no varchar(64) not null,
    file_no varchar(64) not null,
    business_app_code varchar(80) not null,
    uploader_user_id bigint not null,
    expected_content_type varchar(120) not null,
    expected_size_bytes bigint not null,
    expected_file_category varchar(20) not null,
    expires_at timestamp not null,
    status varchar(20) not null,
    created_time timestamp not null,
    modified_time timestamp not null,
    constraint uk_file_upload_session_no unique (upload_session_no),
    constraint fk_file_upload_session_file_no foreign key (file_no) references file_object (file_no)
);

create index if not exists idx_file_upload_session_file_no
    on file_upload_session (file_no);

create index if not exists idx_file_upload_session_expire
    on file_upload_session (status, expires_at);
