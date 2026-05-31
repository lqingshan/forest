-- 初始化内置平台企业。
--
-- 普通商家企业通过 Java 用例创建：
-- organization -> 默认部门 -> owner member -> OrganizationCreatedEvent -> RBAC 默认角色初始化。
--
-- 平台公司不是用户在商家端创建的企业，而是 platform-web 登录准入和平台治理权限的系统内置组织。
-- PlatformLoginAccessGuard / PlatformAccessContextInterceptor 会根据 forest.platform.organization-no
-- 找到该企业，并要求当前 user 是这个企业的 ACTIVE organization_member。
--
-- 本脚本只负责创建 ORG_PLATFORM、默认部门和平台 owner member。
-- 后续 access 脚本会基于这些数据初始化 ORGANIZATION:{平台企业ID} 和 PLATFORM:0 的角色授权。
insert into organization (
    organization_no,
    organization_name,
    status,
    certification_status,
    current_certification_id,
    owner_user_id,
    created_id,
    modified_id,
    deleted,
    created_time,
    modified_time
)
select
    'ORG_PLATFORM',
    '平台公司',
    'ACTIVE',
    'APPROVED',
    null,
    platform_user.user_id,
    platform_user.user_id,
    platform_user.user_id,
    0,
    current_timestamp,
    current_timestamp
from (
    select ua.user_id
    from user_account ua
    join account a on a.id = ua.account_id
    where a.type = 'platform_password'
      and a.credential_scope = 'GLOBAL'
      and a.identifier = '+8618257147892'
      and ua.deleted = 0
      and a.deleted = 0
    order by ua.user_id
    limit 1
) platform_user
where not exists (
    select 1
    from organization
    where organization_no = 'ORG_PLATFORM'
);

insert into organization_department (
    department_no,
    organization_id,
    parent_id,
    department_name,
    default_department,
    sort_order,
    status,
    created_id,
    modified_id,
    deleted,
    created_time,
    modified_time
)
select
    'DEP_PLATFORM_DEFAULT',
    platform_organization.id,
    null,
    '默认部门',
    true,
    0,
    'ACTIVE',
    platform_organization.owner_user_id,
    platform_organization.owner_user_id,
    0,
    current_timestamp,
    current_timestamp
from organization platform_organization
where platform_organization.organization_no = 'ORG_PLATFORM'
  and not exists (
      select 1
      from organization_department existing
      where existing.organization_id = platform_organization.id
        and existing.default_department = true
        and existing.deleted = 0
  );

insert into organization_member (
    member_no,
    organization_id,
    user_id,
    department_id,
    role,
    status,
    joined_time,
    created_id,
    modified_id,
    deleted,
    created_time,
    modified_time
)
select
    'MBR_PLATFORM_ADMIN',
    platform_organization.id,
    platform_organization.owner_user_id,
    default_department.id,
    'OWNER',
    'ACTIVE',
    current_timestamp,
    platform_organization.owner_user_id,
    platform_organization.owner_user_id,
    0,
    current_timestamp,
    current_timestamp
from organization platform_organization
join organization_department default_department
  on default_department.organization_id = platform_organization.id
 and default_department.default_department = true
 and default_department.deleted = 0
where platform_organization.organization_no = 'ORG_PLATFORM'
  and not exists (
      select 1
      from organization_member existing
      where existing.organization_id = platform_organization.id
        and existing.user_id = platform_organization.owner_user_id
        and existing.deleted = 0
  );
