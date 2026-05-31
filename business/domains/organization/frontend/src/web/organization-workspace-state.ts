import { computed, reactive } from 'vue'
import { enterOrganizationWorkspace, fetchMyOrganizationPermissions, listMyOrganizations } from './api'
import type { Organization, OrganizationPermissions } from '../shared/types'

/**
 * 创建企业工作台前端状态的配置。
 */
export interface CreateOrganizationWorkspaceStateOptions {
  /**
   * 当前企业编号的本地缓存 key。
   *
   * 每个 app 必须使用独立 key，避免多个后台应用在同一浏览器里互相污染当前企业上下文。
   */
  storageKey: string

  /**
   * 企业列表加载后，如果没有有效的已选企业，是否自动选择第一家企业。
   *
   * 默认开启，保持已有商家端行为不变。企业入口页和工作台明确分开的 app 可以关闭，
   * 让用户必须主动点击企业后才进入工作台。
   */
  autoSelectFirstOrganization?: boolean
}

export interface RefreshOrganizationsOptions {
  /**
   * 是否强制重新请求我的企业列表。
   *
   * 默认情况下企业列表只初始化一次；创建企业、认证状态变化、切换登录用户等场景需要强制刷新。
   */
  force?: boolean

  /**
   * 刷新企业列表后是否同步刷新当前企业权限。
   *
   * 企业入口页只需要展示“我的企业/创建企业”，可以关闭权限刷新；工作台路由必须开启，
   * 因为菜单、按钮和路由都依赖当前企业权限集合。
   */
  refreshPermissions?: boolean
}

export interface SelectOrganizationOptions {
  /**
   * 选中企业后是否立即刷新企业工作台权限。
   *
   * 批量刷新企业列表时会先选中企业，再统一刷新权限，避免重复请求。
   */
  refreshPermissions?: boolean
}

/**
 * 创建 admin/merchant 类企业工作台的前端运行时状态。
 *
 * 该状态只负责前端交互体验：维护我的企业列表、当前企业编号、当前企业权限集合和加载状态。
 * 后端仍然会在每次 workspace 请求中根据 token + X-Organization-No 重新校验 member 身份，
 * 所以前端状态不是安全边界。
 */
export function createOrganizationWorkspaceState(options: CreateOrganizationWorkspaceStateOptions) {
  const autoSelectFirstOrganization = options.autoSelectFirstOrganization ?? true

  /**
   * 正在进行中的企业列表刷新请求。
   *
   * 路由守卫、页面 mounted、用户操作可能同时触发刷新；用同一个 Promise 合并并发请求，
   * 避免短时间内重复调用 /api/admin/organization/my。
   */
  let refreshOrganizationsPromise: Promise<void> | null = null

  /**
   * 正在进行中的权限刷新请求。
   *
   * 权限刷新包含 enter + my-permissions 两步，同一个企业编号下的并发刷新会被合并。
   */
  let refreshOrganizationPermissionsPromise: Promise<void> | null = null
  let refreshingPermissionsOrganizationNo = ''

  /**
   * 状态版本号。
   *
   * resetOrganizations 会递增版本号；异步请求返回时如果版本已变化，说明登录用户或企业上下文
   * 已被清空，旧请求结果不能再写回当前状态。
   */
  let organizationStateVersion = 0

  const organizationState = reactive({
    organizations: [] as Organization[],
    selectedOrganizationNo: readSelectedOrganizationNo(options.storageKey),
    permissions: null as OrganizationPermissions | null,
    loading: false,
    loaded: false
  })

  const selectedOrganization = computed(() => organizationState.organizations.find(
    (organization) => organization.organizationNo === organizationState.selectedOrganizationNo
  ) ?? null)

  /**
   * 当前企业是否已认证通过。
   *
   * 前端用它控制菜单和路由可见性；后端真正的认证 Gate 仍由 workspace interceptor/aspect 兜底。
   */
  const selectedOrganizationCertified = computed(() => selectedOrganization.value?.certificationStatus === 'APPROVED')

  /**
   * 当前 member 在当前企业工作台中的权限集合。
   *
   * 前端 can() 会读取这个 Set 控制菜单、按钮和路由；接口安全仍以后端 @RequirePermission 为准。
   */
  const permissionSet = computed(() => new Set(organizationState.permissions?.permissions ?? []))

  /**
   * 刷新我的企业列表。
   *
   * 普通进入页面时复用缓存；如果当前已有企业列表且不强制刷新，只在缺少权限时补一次权限。
   * 创建企业后需要传入 force: true，确保新企业出现在列表里。
   */
  async function refreshOrganizations(refreshOptions: RefreshOrganizationsOptions = {}) {
    const shouldRefreshPermissions = refreshOptions.refreshPermissions ?? true

    // 企业列表已经加载过，并且本次没有要求强制刷新时，不再重复请求 /api/admin/organization/my。
    //
    // 这个分支主要服务路由守卫和页面 mounted：
    // 用户在多个私有页面之间跳转时，不应该每次都重新拉企业列表。
    if (organizationState.loaded && !refreshOptions.force) {
      // 企业列表可以复用，但权限数据可能还没有。
      //
      // 典型场景：
      // 1. 用户刚选择企业，selectOrganization 已经清空旧 permissions。
      // 2. 登录态恢复后企业列表存在，但 my-permissions 还没拉取成功。
      // 3. 上一次权限请求失败，当前只剩 selectedOrganizationNo。
      //
      // 因此这里补一次权限刷新，保证后续菜单、按钮、路由 can() 有数据可用。
      if (shouldRefreshPermissions && organizationState.selectedOrganizationNo && !organizationState.permissions) {
        await refreshOrganizationPermissions()
      }
      return
    }

    // 如果企业列表刷新已经在进行中，直接等待同一个 Promise。
    if (refreshOrganizationsPromise) {
      await refreshOrganizationsPromise
      return
    }

    const refreshVersion = organizationStateVersion
    const refreshPromise = doRefreshOrganizations(refreshVersion, shouldRefreshPermissions)
    refreshOrganizationsPromise = refreshPromise
    try {
      await refreshPromise
    } finally {
      // 只清理当前这一次创建的 Promise。
      //
      // 如果 finally 执行时 refreshOrganizationsPromise 已经被后续请求替换，
      // 说明新的刷新流程已经开始，此时不能把新的 Promise 清空。
      if (refreshOrganizationsPromise === refreshPromise) {
        refreshOrganizationsPromise = null
      }
    }
  }

  /**
   * 清空企业工作台前端状态。
   *
   * 退出登录、token 失效、切换登录用户时必须调用，防止旧用户选择的企业编号和权限残留到新用户。
   */
  function resetOrganizations() {
    organizationStateVersion += 1
    refreshOrganizationsPromise = null
    refreshOrganizationPermissionsPromise = null
    refreshingPermissionsOrganizationNo = ''
    organizationState.organizations = []
    organizationState.selectedOrganizationNo = ''
    organizationState.permissions = null
    organizationState.loading = false
    organizationState.loaded = false
    if (typeof window !== 'undefined') {
      window.localStorage.removeItem(options.storageKey)
    }
  }

  /**
   * 执行企业列表刷新主体逻辑。
   *
   * 如果 localStorage 中的 selectedOrganizationNo 不在最新企业列表里，会根据配置决定是否选择第一家企业。
   * 默认行为是“没有有效已选企业时选择第一家”；关闭自动选择后，会保留空选择，等待用户主动进入企业。
   */
  async function doRefreshOrganizations(refreshVersion: number, refreshPermissions: boolean) {
    organizationState.loading = true
    try {
      const organizations = await listMyOrganizations()
      if (refreshVersion !== organizationStateVersion) {
        return
      }
      organizationState.organizations = organizations
      if (!organizationState.organizations.some((item) => item.organizationNo === organizationState.selectedOrganizationNo)) {
        selectOrganization(
          autoSelectFirstOrganization ? organizationState.organizations[0]?.organizationNo ?? '' : '',
          { refreshPermissions: false }
        )
      }
      if (refreshPermissions) {
        await refreshOrganizationPermissions()
      }
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
   * 选择当前企业。
   *
   * 选择结果会写入 localStorage。后续 workspace API 调用会通过 organization web API
   * 自动携带 X-Organization-No；如果 refreshPermissions=true，会立即重新 enter 并拉取权限。
   */
  function selectOrganization(organizationNo: string, selectOptions: SelectOrganizationOptions = {}) {
    const refreshPermissions = selectOptions.refreshPermissions ?? true
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
        window.localStorage.setItem(options.storageKey, organizationNo)
      } else {
        window.localStorage.removeItem(options.storageKey)
      }
    }
    if (refreshPermissions) {
      void refreshOrganizationPermissions().catch(() => undefined)
    }
  }

  /**
   * 刷新当前企业权限。
   *
   * 没有当前企业时清空权限；有当前企业时会合并相同 organizationNo 的并发请求。
   */
  async function refreshOrganizationPermissions() {
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

  /**
   * 进入企业工作台并拉取我的权限。
   *
   * 第一步 enterOrganizationWorkspace 用于服务端校验 user 是否是该企业 ACTIVE member；
   * 第二步 fetchMyOrganizationPermissions 用于拉取当前 member 在 ORGANIZATION:{organizationId}
   * 边界下的权限集合。只有请求期间状态版本和当前企业仍匹配，结果才会写回。
   */
  async function doRefreshOrganizationPermissions(organizationNo: string, refreshVersion: number) {
    await enterOrganizationWorkspace(organizationNo)
    const permissions = await fetchMyOrganizationPermissions(organizationNo)
    if (refreshVersion === organizationStateVersion && organizationState.selectedOrganizationNo === organizationNo) {
      organizationState.permissions = permissions
    }
  }

  /**
   * 前端权限判断。
   *
   * 只用于菜单、按钮、路由体验控制，不替代后端 @RequirePermission。
   */
  function can(permissionCode: string) {
    return permissionSet.value.has(permissionCode)
  }

  return {
    organizationState,
    selectedOrganization,
    selectedOrganizationCertified,
    permissionSet,
    refreshOrganizations,
    resetOrganizations,
    selectOrganization,
    refreshOrganizationPermissions,
    can
  }
}

function readSelectedOrganizationNo(storageKey: string) {
  if (typeof window === 'undefined') {
    return ''
  }
  return window.localStorage.getItem(storageKey) ?? ''
}
