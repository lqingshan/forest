insert into access_role (
    role_code,
    role_name,
    boundary_type,
    boundary_id,
    system_preset,
    status,
    created_id,
    modified_id,
    deleted,
    created_time,
    modified_time
)
select role_code, role_name, 'ORGANIZATION', organization_id, true, 'ACTIVE', owner_user_id, owner_user_id, 0, now(), now()
from (
    select id as organization_id, owner_user_id, 'organization_owner' as role_code, '企业所有者' as role_name from organization where deleted = 0
    union all
    select id as organization_id, owner_user_id, 'organization_admin' as role_code, '企业管理员' as role_name from organization where deleted = 0
    union all
    select id as organization_id, owner_user_id, 'organization_member' as role_code, '普通员工' as role_name from organization where deleted = 0
) source
where not exists (
    select 1 from access_role existing
    where existing.boundary_type = 'ORGANIZATION'
      and existing.boundary_id = source.organization_id
      and existing.role_code = source.role_code
      and existing.deleted = 0
);

insert into access_role (
    role_code,
    role_name,
    boundary_type,
    boundary_id,
    system_preset,
    status,
    created_id,
    modified_id,
    deleted,
    created_time,
    modified_time
)
select
    'platform_super_admin',
    '平台超级管理员',
    'PLATFORM',
    0,
    true,
    'ACTIVE',
    organization.owner_user_id,
    organization.owner_user_id,
    0,
    now(),
    now()
from organization
where organization.organization_no = 'ORG_PLATFORM'
  and organization.deleted = 0
  and not exists (
      select 1
      from access_role existing
      where existing.boundary_type = 'PLATFORM'
        and existing.boundary_id = 0
        and existing.role_code = 'platform_super_admin'
        and existing.deleted = 0
  );

insert into access_role_permission (
    role_id,
    permission_pattern,
    pattern_type,
    created_id,
    modified_id,
    deleted,
    created_time,
    modified_time
)
select role_id, permission_pattern, pattern_type, created_id, created_id, 0, now(), now()
from (
    select role.id as role_id, 'organization.*' as permission_pattern, 'WILDCARD' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_owner' and role.deleted = 0
    union all
    select role.id as role_id, 'access.*' as permission_pattern, 'WILDCARD' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_owner' and role.deleted = 0
    union all
    select role.id as role_id, 'organization.read' as permission_pattern, 'EXACT' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_admin' and role.deleted = 0
    union all
    select role.id as role_id, 'organization.update' as permission_pattern, 'EXACT' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_admin' and role.deleted = 0
    union all
    select role.id as role_id, 'organization.certification.submit' as permission_pattern, 'EXACT' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_admin' and role.deleted = 0
    union all
    select role.id as role_id, 'organization.department.*' as permission_pattern, 'WILDCARD' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_admin' and role.deleted = 0
    union all
    select role.id as role_id, 'organization.member.*' as permission_pattern, 'WILDCARD' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_admin' and role.deleted = 0
    union all
    select role.id as role_id, 'organization.read' as permission_pattern, 'EXACT' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_member' and role.deleted = 0
    union all
    select role.id as role_id, 'organization.department.read' as permission_pattern, 'EXACT' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_member' and role.deleted = 0
    union all
    select role.id as role_id, 'organization.member.read' as permission_pattern, 'EXACT' as pattern_type, role.created_id
    from access_role role
    where role.role_code = 'organization_member' and role.deleted = 0
    union all
    select role.id as role_id, 'platform.*' as permission_pattern, 'WILDCARD' as pattern_type, role.created_id
    from access_role role
    where role.boundary_type = 'PLATFORM' and role.role_code = 'platform_super_admin' and role.deleted = 0
) source
where not exists (
    select 1 from access_role_permission existing
    where existing.role_id = source.role_id
      and existing.permission_pattern = source.permission_pattern
      and existing.deleted = 0
);

insert into access_role_assignment (
    subject_type,
    subject_id,
    boundary_type,
    boundary_id,
    role_id,
    created_id,
    modified_id,
    deleted,
    created_time,
    modified_time
)
select
    'ORGANIZATION_MEMBER',
    member.id,
    'ORGANIZATION',
    member.organization_id,
    role.id,
    member.created_id,
    member.modified_id,
    0,
    now(),
    now()
from organization_member member
join access_role role
  on role.boundary_type = 'ORGANIZATION'
 and role.boundary_id = member.organization_id
 and role.role_code = case member.role
     when 'OWNER' then 'organization_owner'
     when 'ADMIN' then 'organization_admin'
     else 'organization_member'
 end
 and role.deleted = 0
where member.deleted = 0
  and not exists (
      select 1 from access_role_assignment existing
      where existing.subject_type = 'ORGANIZATION_MEMBER'
        and existing.subject_id = member.id
        and existing.boundary_type = 'ORGANIZATION'
        and existing.boundary_id = member.organization_id
        and existing.role_id = role.id
        and existing.deleted = 0
  );

insert into access_role_assignment (
    subject_type,
    subject_id,
    boundary_type,
    boundary_id,
    role_id,
    created_id,
    modified_id,
    deleted,
    created_time,
    modified_time
)
select
    'ORGANIZATION_MEMBER',
    member.id,
    'PLATFORM',
    0,
    role.id,
    member.created_id,
    member.modified_id,
    0,
    now(),
    now()
from organization
join organization_member member
  on member.organization_id = organization.id
 and member.user_id = organization.owner_user_id
 and member.deleted = 0
join access_role role
  on role.boundary_type = 'PLATFORM'
 and role.boundary_id = 0
 and role.role_code = 'platform_super_admin'
 and role.deleted = 0
where organization.organization_no = 'ORG_PLATFORM'
  and organization.deleted = 0
  and not exists (
      select 1 from access_role_assignment existing
      where existing.subject_type = 'ORGANIZATION_MEMBER'
        and existing.subject_id = member.id
        and existing.boundary_type = 'PLATFORM'
        and existing.boundary_id = 0
        and existing.role_id = role.id
        and existing.deleted = 0
  );
