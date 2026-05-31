alter table file_object
    add column if not exists created_id bigint;

alter table file_object
    add column if not exists modified_id bigint;

alter table file_upload_session
    add column if not exists created_id bigint;

alter table file_upload_session
    add column if not exists modified_id bigint;

alter table file_upload_session
    add column if not exists deleted integer not null default 0;
