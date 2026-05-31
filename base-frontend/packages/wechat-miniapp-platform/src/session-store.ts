import { getRuntime, type MiniappRecord } from './runtime'
import { createMiniappStorage, type CreateMiniappStorageOptions, type MiniappStorage } from './storage'

/**
 * 小程序登录态仓库。
 *
 * 这个模块只负责 token/currentUser 的存储和同步：
 * - 不调用 wx.login
 * - 不请求业务 API
 * - 不处理页面跳转
 *
 * 登录编排由 client-session 公共包负责；
 * 具体 app 再把自己的用户登录 API 注入进去。
 */

/**
 * MiniappSessionStore 只负责“登录态存取”，不负责“怎么登录”。
 *
 * 它管理三类状态：
 * - accessToken
 * - refreshToken
 * - currentUser
 *
 * 状态同时存在两份：
 * - 内存 state：当前 JS bundle 中快速读取
 * - wx storage：小程序冷启动后可以恢复
 *
 * 它还会把 currentUser 同步到 App.globalData，方便传统小程序页面读取。
 */
export interface MiniappSessionStore {
  /**
   * 分类：恢复。
   * 冷启动时从 wx storage 恢复 accessToken/refreshToken/currentUser 到内存 state。
   */
  hydrateSessionStore(): void
  /**
   * 分类：同步。
   * 已经恢复后，再检查 storage 是否被其它页面 bundle 更新，并同步到内存。
   */
  syncPersistedSessionStore(): void
  /**
   * 分类：保护入口。
   * 确保读写 session 前，状态已经完成恢复或同步。
   */
  ensureSessionStoreHydrated(): void
  /**
   * 分类：读取。
   * 获取接口请求使用的 accessToken。
   */
  getAccessToken(): string
  /**
   * 分类：读取。
   * 获取刷新 accessToken 使用的 refreshToken。
   */
  getRefreshToken(): string
  /**
   * 分类：读取。
   * 获取缓存用户，不主动请求后端。
   */
  getCachedUser<TUser = MiniappRecord>(): TUser | null
  /**
   * 分类：读取。
   * 获取 session 版本号，用于判断异步期间登录态是否变化。
   */
  getSessionRevision(): number
  /**
   * 分类：校验。
   * 判断传入 token 是否仍然是 wx storage 中的当前 accessToken。
   */
  isPersistedAccessToken(accessToken: string): boolean
  /**
   * 分类：保存。
   * 保存 accessToken/refreshToken 到内存 state 和 wx storage。
   */
  saveSessionTokens(accessToken: string, refreshToken?: string): void
  /**
   * 分类：保存。
   * 保存 currentUser 到内存 state、wx storage 和 App.globalData。
   */
  saveCachedUser(user: MiniappRecord | null): void
  /**
   * 分类：安全保存。
   * 只有 token 没变时才保存 currentUser，避免旧异步请求覆盖新登录态。
   */
  saveCachedUserIfTokenMatches(user: MiniappRecord, accessToken: string): boolean
  /**
   * 分类：清理。
   * 清理登录态；非 force 场景会保护 storage 中已经写入的新 token。
   */
  clearSessionStore(options?: ClearSessionStoreOptions): boolean
}

/**
 * force 用于“用户主动退出登录”等场景。
 *
 * 非 force 清理会保护“更新的 token”：如果一个旧请求返回 401，
 * 但用户已经重新登录并写入了新 token，旧请求不能把新登录态清掉。
 */
export interface ClearSessionStoreOptions {
  force?: boolean
}

/**
 * 创建 session store 时可以注入 storage，也可以让它自己创建 wx storage。
 * currentUserGlobalDataKey 用来控制 currentUser 写入 App.globalData 的字段名。
 */
export interface CreateSessionStoreOptions extends CreateMiniappStorageOptions {
  storage?: MiniappStorage
  currentUserGlobalDataKey?: string
}

/**
 * session store 的内部状态。
 *
 * revision 是一个“版本号”：token/currentUser 变化时递增。
 * 它可以帮助上层判断异步请求返回时，当前 session 是否已经变过。
 */
interface SessionStoreState {
  accessToken: string
  refreshToken: string
  currentUser: MiniappRecord | null
  hydrated: boolean
  revision: number
}

const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const CURRENT_USER_KEY = 'currentUser'

/**
 * 创建小程序登录态仓库。
 */
export function createSessionStore(options: CreateSessionStoreOptions): MiniappSessionStore {
  const storage = options.storage || createMiniappStorage(options)
  const currentUserGlobalDataKey = options.currentUserGlobalDataKey || CURRENT_USER_KEY
  const state: SessionStoreState = {
    accessToken: '',
    refreshToken: '',
    currentUser: null,
    hydrated: false,
    revision: 0
  }

  function syncAppCurrentUser() {
    try {
      // App 早期启动阶段 getApp 可能不可用，所以这里是 best-effort 同步。
      const app = getRuntime(options.runtime).getApp?.()
      if (app?.globalData) {
        app.globalData[currentUserGlobalDataKey] = state.currentUser
      }
    } catch {
      // App may be unavailable during early bootstrap.
    }
  }

  function clearPersistedSession() {
    // 清理持久化登录态：小程序重启后也不会再恢复这些 token/user。
    storage.setString(ACCESS_TOKEN_KEY, '')
    storage.setString(REFRESH_TOKEN_KEY, '')
    storage.setJson(CURRENT_USER_KEY, null)
  }

  function clearInMemorySession() {
    // revision 递增表示 session 已变化，上层可以用它识别过期异步结果。
    state.revision += 1
    state.accessToken = ''
    state.refreshToken = ''
    state.currentUser = null
    syncAppCurrentUser()
  }

  function shouldClearPersistedSession(force: boolean) {
    if (force) {
      // 用户主动退出登录时必须清理 storage，不管当前 storage 里是什么 token。
      return true
    }

    // 非 force 场景通常来自 401。这里保护新登录态：
    // 如果 storage 里的 token 已经不是当前内存 token，说明别的流程已经写入新 token，
    // 当前旧请求不能把新 token 清掉。
    const persistedAccessToken = storage.getString(ACCESS_TOKEN_KEY)
    return !persistedAccessToken || persistedAccessToken === state.accessToken
  }

  const store: MiniappSessionStore = {
    hydrateSessionStore() {
      // hydrate = 首次从 storage 恢复到内存 state。
      state.accessToken = storage.getString(ACCESS_TOKEN_KEY)
      state.refreshToken = storage.getString(REFRESH_TOKEN_KEY)
      state.currentUser = storage.getJson<MiniappRecord>(CURRENT_USER_KEY)
      state.hydrated = true
      syncAppCurrentUser()
    },
    syncPersistedSessionStore() {
      // 已经 hydrate 后，每次读取前再检查 storage 是否被其它流程改过。
      // 小程序多入口 bundle 下，可能不同页面各自有一份 JS 状态；
      // storage 是它们之间的共享事实来源。
      const persistedAccessToken = storage.getString(ACCESS_TOKEN_KEY)
      const persistedRefreshToken = storage.getString(REFRESH_TOKEN_KEY)
      if (persistedAccessToken === state.accessToken && persistedRefreshToken === state.refreshToken) {
        return
      }

      state.revision += 1
      state.accessToken = persistedAccessToken
      state.refreshToken = persistedRefreshToken
      state.currentUser = storage.getJson<MiniappRecord>(CURRENT_USER_KEY)
      syncAppCurrentUser()
    },
    ensureSessionStoreHydrated() {
      // 所有读取入口都先确保内存 state 与 storage 对齐。
      if (!state.hydrated) {
        store.hydrateSessionStore()
        return
      }

      store.syncPersistedSessionStore()
    },
    getAccessToken() {
      // HTTP client 会通过这个方法拿 token，并注入 Authorization header。
      store.ensureSessionStoreHydrated()
      return state.accessToken
    },
    getRefreshToken() {
      store.ensureSessionStoreHydrated()
      return state.refreshToken
    },
    getCachedUser<TUser = MiniappRecord>() {
      store.ensureSessionStoreHydrated()
      return state.currentUser as TUser | null
    },
    getSessionRevision() {
      // 上层可用 revision 防止旧异步请求覆盖新 session。
      store.ensureSessionStoreHydrated()
      return state.revision
    },
    isPersistedAccessToken(accessToken: string) {
      // 判断某个异步流程拿到的 accessToken 是否还是当前 storage 中的 token。
      return Boolean(accessToken) && storage.getString(ACCESS_TOKEN_KEY) === accessToken
    },
    saveSessionTokens(accessToken: string, refreshToken?: string) {
      store.ensureSessionStoreHydrated()
      const nextAccessToken = accessToken || ''
      const nextRefreshToken = refreshToken || state.refreshToken || ''
      if (state.accessToken !== nextAccessToken || state.refreshToken !== nextRefreshToken) {
        state.revision += 1
      }
      state.accessToken = nextAccessToken
      state.refreshToken = nextRefreshToken
      storage.setString(ACCESS_TOKEN_KEY, state.accessToken)
      storage.setString(REFRESH_TOKEN_KEY, state.refreshToken)
    },
    saveCachedUser(user: MiniappRecord | null) {
      // currentUser 同时写入内存、storage、App.globalData。
      store.ensureSessionStoreHydrated()
      state.currentUser = user || null
      storage.setJson(CURRENT_USER_KEY, state.currentUser)
      syncAppCurrentUser()
    },
    saveCachedUserIfTokenMatches(user: MiniappRecord, accessToken: string) {
      // 登录/恢复用户信息是异步的。保存用户信息前再次确认 token 未变化，
      // 防止“旧 token 拉回来的用户”覆盖“新 token 的用户”。
      if (!store.isPersistedAccessToken(accessToken)) {
        return false
      }

      store.saveCachedUser(user)
      return true
    },
    clearSessionStore(options: ClearSessionStoreOptions = {}) {
      if (!state.hydrated) {
        store.hydrateSessionStore()
      }
      // clearPersisted 表示这次是否真的清掉了 storage。
      // 返回这个布尔值便于测试和上层判断。
      const clearPersisted = shouldClearPersistedSession(Boolean(options.force))
      clearInMemorySession()
      if (clearPersisted) {
        clearPersistedSession()
      }
      return clearPersisted
    }
  }

  return store
}
