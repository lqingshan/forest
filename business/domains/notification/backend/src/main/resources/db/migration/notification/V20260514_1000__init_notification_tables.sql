create table if not exists sms_send_log (
    id bigserial primary key,
    sms_no varchar(40) not null unique,
    business_app_code varchar(80) not null,
    client_app_code varchar(80),
    client_type varchar(30),
    scene varchar(40) not null,
    phone varchar(30) not null,
    template_code varchar(80),
    sign_name varchar(80),
    content_snapshot varchar(500),
    provider varchar(30) not null,
    provider_request_id varchar(120),
    provider_biz_id varchar(120),
    provider_response_code varchar(80),
    provider_response_message varchar(255),
    status varchar(20) not null,
    send_ip varchar(64),
    sent_at timestamp,
    failed_at timestamp,
    created_id bigint,
    modified_id bigint,
    deleted integer not null default 0,
    created_time timestamp not null,
    modified_time timestamp not null
);

create index if not exists idx_sms_send_log_phone_created
    on sms_send_log (phone, created_time);

create index if not exists idx_sms_send_log_app_scene_created
    on sms_send_log (business_app_code, scene, created_time);
