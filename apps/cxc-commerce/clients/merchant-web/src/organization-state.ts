import { computed, reactive } from 'vue'
import { enterOrganizationWorkspace, fetchMyOrganizationPermissions, listMyOrganizations } from '@forest/organization/web'
import type { Organization, OrganizationPermissions } from '@forest/organization/shared'

const SELECTED_ORGANIZATION_KEY = 'forest.cxc-commerce.merchant.selectedOrganizationNo'

interface RefreshOrganizationsOptions {
  force?: boolean
}

interface SelectOrganizationOptions {
  refreshPermissions?: boolean
}

let refreshOrganizationsPromise: Promise<void> | null = null
let refreshOrganizationPermissionsPromise: Promise<void> | null = null
let refreshingPermissionsOrganizationNo = ''
let organizationStateVersion = 0

/**
 * 商家端当前企业工作台状态。
 *
 * 这里保存的是前端本地选择的企业和当前员工权限快照，不是后端可信上下文。
 * 后端仍会在每次 workspace 请求中根据 X-Organization-No 重新校验当前 user 是否是 ACTIVE member。
 */
export const organizationState = reactive({
  /**
   * 当前登录 user 可进入的企业列表，只包含后端返回的有效企业员工身份。
   */
  organizations: [] as Organization[],

  /**
   * 前端当前选择的企业编号，会持久化到 localStorage，用于刷新页面后恢复工作台选择。
   */
  selectedOrganizationNo: readSelectedOrganizationNo(),

  /**
   * 当前选择企业下的工作台权限与状态。
   *
   * 该数据来自 /workspace/access/my-permissions，前端用它控制菜单、按钮和路由。
   */
  permissions: null as OrganizationPermissions | null,

  /**
   * 企业列表与权限刷新中的 UI 状态。
   */
  loading: false,

  /**
   * 企业上下文是否已经完成过一次初始化。
   */
  loaded: false
})

/**
 * 当前被选中的企业。
 */
export const selectedOrganization = computed(() => organizationState.organizations.find(
  (organization) => organization.organizationNo === organizationState.selectedOrganizationNo
) ?? null)

/**
 * 当前企业是否已经认证通过。
 *
 * 未认证企业只展示企业资料、认证提交/状态等受限功能，完整后台功能还要同时结合权限判断。
 */
export const selectedOrganizationCertified = computed(() => selectedOrganization.value?.certificationStatus === 'APPROVED')

/**
 * 当前员工在当前企业工作台下拥有的精确权限点集合。
 */
export const permissionSet = computed(() => new Set(organizationState.permissions?.permissions ?? []))

/**
 * 刷新“我的企业”列表，并在当前选择失效时自动切换到第一家可用企业。
 *
 * 默认只初始化一次，避免路由跳转、布局挂载、页面挂载同时造成重复请求。
 * 创建企业、认证状态变化等业务动作后传入 force=true，可强制重拉列表和权限。
 */
export async function refreshOrganizations(options: RefreshOrganizationsOptions = {}) {
  if (organizationState.loaded && !options.force) {
    if (organizationState.selectedOrganizationNo && !organizationState.permissions) {
      await refreshOrganizationPermissions()
    }
    return
  }
  if (refreshOrganizationsPromise) {
    await refreshOrganizationsPromise
    return
  }
  const refreshVersion = organizationStateVersion
  const refreshPromise = doRefreshOrganizations(refreshVersion)
  refreshOrganizationsPromise = refreshPromise
  try {
    await refreshPromise
  } finally {
    if (refreshOrganizationsPromise === refreshPromise) {
      refreshOrganizationsPromise = null
    }
  }
}

/**
 * 清理企业工作台上下文，通常用于退出登录或登录用户切换。
 */
export function resetOrganizations() {
  organizationStateVersion += 1
  refreshOrganizationsPromise = null
  refreshOrganizationPermissionsPromise = null
  refreshingPermissionsOrganizationNo = ''
  organizationState.organizations = []
  organizationState.permissions = null
  organizationState.loading = false
  organizationState.loaded = false
}

async function doRefreshOrganizations(refreshVersion: number) {
  organizationState.loading = true
  try {
    const organizations = await listMyOrganizations()
    if (refreshVersion !== organizationStateVersion) {
      return
    }
    organizationState.organizations = organizations
    if (!organizationState.organizations.some((item) => item.organizationNo === organizationState.selectedOrganizationNo)) {
      selectOrganization(organizationState.organizations[0]?.organizationNo ?? '', { refreshPermissions: false })
    }
    await refreshOrganizationPermissions()
    if (refreshVersion === organizationStateVersion) {
      organizationState.loaded = true
    }
  } finally {
    if (refreshVersion === organizationStateVersion) {
      organizationState.loading = false
    }
  }
}

/**
 * 切换当前企业工作台。
 *
 * 选择结果只保存在前端本地状态和 localStorage；真正的企业准入仍由后端 enter/workspace 接口校验。
 */
export function selectOrganization(organizationNo: string, options: SelectOrganizationOptions = {}) {
  const refreshPermissions = options.refreshPermissions ?? true
  if (organizationState.selectedOrganizationNo === organizationNo) {
    if (refreshPermissions && organizationNo && !organizationState.permissions) {
      void refreshOrganizationPermissions().catch(() => undefined)
    }
    return
  }
  organizationState.selectedOrganizationNo = organizationNo
  organizationState.permissions = null
  if (typeof window !== 'undefined') {
    if (organizationNo) {
      window.localStorage.setItem(SELECTED_ORGANIZATION_KEY, organizationNo)
    } else {
      window.localStorage.removeItem(SELECTED_ORGANIZATION_KEY)
    }
  }
  if (refreshPermissions) {
    void refreshOrganizationPermissions().catch(() => undefined)
  }
}

/**
 * 刷新当前企业的工作台状态和权限集合。
 *
 * enter 接口用于确认当前 user 仍可进入该企业；my-permissions 返回前端菜单/按钮需要的权限快照。
 */
export async function refreshOrganizationPermissions() {
  const organizationNo = organizationState.selectedOrganizationNo
  if (!organizationNo) {
    organizationState.permissions = null
    return
  }
  if (refreshOrganizationPermissionsPromise && refreshingPermissionsOrganizationNo === organizationNo) {
    await refreshOrganizationPermissionsPromise
    return
  }
  const refreshVersion = organizationStateVersion
  const refreshPromise = doRefreshOrganizationPermissions(organizationNo, refreshVersion)
  refreshingPermissionsOrganizationNo = organizationNo
  refreshOrganizationPermissionsPromise = refreshPromise
  try {
    await refreshPromise
  } finally {
    if (refreshOrganizationPermissionsPromise === refreshPromise) {
      refreshOrganizationPermissionsPromise = null
      refreshingPermissionsOrganizationNo = ''
    }
  }
}

async function doRefreshOrganizationPermissions(organizationNo: string, refreshVersion: number) {
  await enterOrganizationWorkspace(organizationNo)
  const permissions = await fetchMyOrganizationPermissions(organizationNo)
  if (refreshVersion === organizationStateVersion && organizationState.selectedOrganizationNo === organizationNo) {
    organizationState.permissions = permissions
  }
}

/**
 * 判断当前员工是否拥有某个精确权限点。
 *
 * 通配符只存在于后端角色授权配置，前端 can() 只接收后端展开后的精确权限点。
 */
export function can(permissionCode: string) {
  return permissionSet.value.has(permissionCode)
}

/**
 * 从 localStorage 恢复上次选择的企业编号。
 */
function readSelectedOrganizationNo() {
  if (typeof window === 'undefined') {
    return ''
  }
  return window.localStorage.getItem(SELECTED_ORGANIZATION_KEY) ?? ''
}
