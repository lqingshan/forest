import { reactive } from 'vue'
import {
  apiPathForAccessScope,
  configureHttpForAccessScope,
  httpForAccessScope
} from '@forest/http-client'
import { uploadWebFile, type WebFileUploadOptions } from '@forest/file/web/upload'
import type { User as CurrentUser } from '../../shared/user'
import {
  loginByCarrierToken,
  loginByPassword,
  loginByPhone,
  logout,
  refreshAccessToken
} from './api'
import {
  loginHistoryStorageKey,
  readWebLoginHistory,
  recordPhonePasswordLoginHistory,
  recordPhoneSmsLoginHistory,
  type WebLoginHistory
} from './login-history'
import type {
  CarrierOneClickLoginOptions,
  LoginResult,
  PasswordLoginOptions as PasswordLoginApiOptions,
  PhoneSmsLoginOptions
} from './types'

/**
 * Web/H5 运行时的用户会话工厂。
 *
 * <p>它只处理“当前前端端口如何持有登录态”：token 存储、刷新 token、
 * 恢复当前用户、退出登录、头像上传等。app 层需要为自己的端创建一个
 * userSession 实例，并把它传给登录面板、当前用户组件和路由守卫。</p>
 *
 * <p>这里不做企业、店铺、角色、权限判断；这些属于后续 organization / RBAC
 * 等业务域。accessScope 在这里仅用于选择 API 前缀和 HTTP client。</p>
 */
const DEFAULT_ACCESS_TOKEN_KEY = 'forest.user.web.accessToken'
const DEFAULT_REFRESH_TOKEN_KEY = 'forest.user.web.refreshToken'

/**
 * 当前 Web 会话的响应式状态。
 *
 * <p>布局、头像组件、页面守卫可以读取它来展示当前用户信息或恢复状态。
 * 它属于某一个 WebUserSession 实例，不再是 user 模块的全局单例。</p>
 */
export interface WebUserSessionState {
  /** 当前登录用户资料；未登录或恢复失败时为 null。 */
  currentUser: CurrentUser | null
  /** 是否正在根据本地 token 恢复当前用户，避免并发恢复。 */
  restoring: boolean
}

export interface PhonePasswordLoginOptions extends Partial<PasswordLoginApiOptions> {
  /** 是否保存密码到浏览器本地登录历史；默认保存。 */
  savePassword?: boolean
}

/**
 * app 端创建 WebUserSession 时注入的端上下文。
 *
 * <p>appCode 标识具体客户端应用；clientType 标识运行端类型；
 * accessScope 标识 API 访问面，当前只用于 CLIENT / ADMIN / PLATFORM
 * 三类 API 前缀选择。storagePrefix 用于隔离不同 app 端的 token key。</p>
 */
export interface WebUserSessionOptions {
  /** 客户端应用编码，例如 cxc-commerce-merchant-web。 */
  appCode: string
  /** 客户端类型，例如 PC_WEB / MOBILE_H5 / ANDROID_APP。 */
  clientType: string
  /** API 访问面：CLIENT / ADMIN / PLATFORM。 */
  accessScope: string
  /** token localStorage key 前缀，推荐每个 app 端独立配置。 */
  storagePrefix?: string
  /** 显式覆盖 accessToken 存储 key；通常不需要，优先使用 storagePrefix。 */
  accessTokenKey?: string
  /** 显式覆盖 refreshToken 存储 key；通常不需要，优先使用 storagePrefix。 */
  refreshTokenKey?: string
}

/**
 * app 端持有的用户会话实例。
 *
 * <p>组件和路由守卫只依赖这个接口，不直接读取 user 模块内部状态。
 * 后续新增学生端、兼职端、老师端时，也只需要创建自己的 WebUserSession。</p>
 */
export interface WebUserSession {
  /** 创建 session 时注入的客户端应用编码。 */
  appCode: string
  /** 创建 session 时注入的客户端类型。 */
  clientType: string
  /** 创建 session 时注入的 API 访问面。 */
  accessScope: string
  /** 当前 session 的响应式状态。 */
  state: WebUserSessionState
  /** 从本 session 的 token key 中读取 accessToken。 */
  getAccessToken(): string
  /** 从本 session 的 token key 中读取 refreshToken。 */
  getRefreshToken(): string
  /** 从本 session 的登录历史 key 中读取本地登录历史。 */
  getLoginHistory(): WebLoginHistory
  /** 清理本 session 的 token 和当前用户状态。 */
  clear(): void
  /** 使用 refreshToken 换取新的 accessToken。 */
  refreshAccessToken(): Promise<string | null>
  /** 请求当前 accessScope 对应的 /user/me。 */
  fetchCurrentUser(): Promise<CurrentUser>
  /** 登录接口成功后的统一收口：写 token，并刷新当前用户资料。 */
  applyLoginResult(result: LoginResult): Promise<CurrentUser>
  /** 手机号短信验证码登录。 */
  loginWithPhoneSms(phone: string, smsCode: string, options?: PhoneSmsLoginOptions): Promise<CurrentUser>
  /** 手机号密码登录。 */
  loginWithPassword(phone: string, password: string, options?: PhonePasswordLoginOptions): Promise<CurrentUser>
  /** APP 原生本机号一键登录，carrierToken 由 native bridge 获取。 */
  loginWithCarrierToken(carrierToken: string, options?: CarrierOneClickLoginOptions): Promise<CurrentUser>
  /** 页面刷新后，根据本地 token 恢复当前用户。 */
  restore(): Promise<boolean>
  /** 调用后端 logout，并清理本地 session。 */
  logout(): Promise<void>
  /** 直接把已可用的头像 fileNo 绑定到当前用户。 */
  updateAvatar(fileNo: string): Promise<CurrentUser>
  /** 上传头像文件，complete 后再把返回的 fileNo 绑定到当前用户。 */
  uploadAvatar(file: File, options?: Omit<WebFileUploadOptions, 'file' | 'fileCategory'>): Promise<CurrentUser>
}

/**
 * 创建一个 Web/H5 用户会话实例。
 *
 * <p>每次调用都会创建独立的 Vue state 和 token key，因此 app 层可以用自己的
 * session.ts 固定导出 userSession。当前底层 HTTP client 仍按 accessScope
 * 选择并配置全局 clientHttp / adminHttp / platformHttp，适合“每个 app 端
 * 一个 session 实例”的现有运行方式。</p>
 */
export function createWebUserSession(options: WebUserSessionOptions): WebUserSession {
  const appCode = requireText(options.appCode, 'appCode')
  const clientType = requireText(options.clientType, 'clientType')
  const accessScope = requireText(options.accessScope, 'accessScope')
  const httpClient = httpForAccessScope(accessScope)
  const currentUserApi = `${apiPathForAccessScope(accessScope)}/user/me`
  const accessTokenKey = options.accessTokenKey ?? storageKey(options.storagePrefix, 'accessToken', DEFAULT_ACCESS_TOKEN_KEY)
  const refreshTokenKey = options.refreshTokenKey ?? storageKey(options.storagePrefix, 'refreshToken', DEFAULT_REFRESH_TOKEN_KEY)
  const loginHistoryKey = loginHistoryStorageKey(options.storagePrefix)
  const state = reactive<WebUserSessionState>({
    currentUser: null,
    restoring: false
  })

  const session: WebUserSession = {
    appCode,
    clientType,
    accessScope,
    state,
    getAccessToken,
    getRefreshToken,
    getLoginHistory,
    clear,
    refreshAccessToken: refreshSessionAccessToken,
    fetchCurrentUser,
    applyLoginResult,
    loginWithPhoneSms,
    loginWithPassword,
    loginWithCarrierToken,
    restore,
    logout: logoutSession,
    updateAvatar,
    uploadAvatar
  }

  // 将当前 session 的 token 读取、刷新、401 清理逻辑挂到对应访问面的 HTTP client。
  if (typeof window !== 'undefined') {
    configureHttpForAccessScope(accessScope, {
      getAccessToken: () => getAccessToken() || null,
      refreshAccessToken: refreshSessionAccessToken,
      onUnauthorized: () => {
        if (getAccessToken()) {
          clear()
        }
      }
    })
  }

  return session

  function getAccessToken() {
    if (typeof window === 'undefined') {
      return ''
    }
    return window.localStorage.getItem(accessTokenKey) ?? ''
  }

  function getRefreshToken() {
    if (typeof window === 'undefined') {
      return ''
    }
    return window.localStorage.getItem(refreshTokenKey) ?? ''
  }

  function getLoginHistory() {
    return readWebLoginHistory(loginHistoryKey)
  }

  function setAccessToken(token: string) {
    window.localStorage.setItem(accessTokenKey, token)
  }

  function setRefreshToken(token: string) {
    window.localStorage.setItem(refreshTokenKey, token)
  }

  function clear() {
    if (typeof window !== 'undefined') {
      window.localStorage.removeItem(accessTokenKey)
      window.localStorage.removeItem(refreshTokenKey)
    }
    state.currentUser = null
    state.restoring = false
  }

  // 使用 refreshToken 换新 accessToken；没有 refreshToken 时让 HTTP client 走未登录分支。
  async function refreshSessionAccessToken() {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      return null
    }
    const result = await refreshAccessToken(refreshToken, httpClient)
    setAccessToken(result.accessToken)
    return result.accessToken
  }

  function fetchCurrentUser() {
    return httpClient.get<CurrentUser>(currentUserApi)
  }

  // 登录方式很多，但成功后的动作必须一致：保存 token，再请求当前用户资料。
  // 如果 /user/me 失败，说明 token 对应的访问面不可用，需要清掉半登录态。
  async function applyLoginResult(result: LoginResult) {
    setAccessToken(result.accessToken)
    setRefreshToken(result.refreshToken)
    try {
      state.currentUser = await fetchCurrentUser()
      return state.currentUser
    } catch (error) {
      clear()
      throw error
    }
  }

  async function loginWithPhoneSms(phone: string, smsCode: string, loginOptions?: PhoneSmsLoginOptions) {
    const result = await loginByPhone(phone, smsCode, loginOptions ?? sessionLoginOptions(), httpClient)
    const user = await applyLoginResult(result)
    recordPhoneSmsLoginHistory(loginHistoryKey, phone)
    return user
  }

  async function loginWithPassword(phone: string, password: string, loginOptions?: PhonePasswordLoginOptions) {
    const result = await loginByPassword(phone, password, sessionPasswordLoginOptions(loginOptions), httpClient)
    const user = await applyLoginResult(result)
    recordPhonePasswordLoginHistory(loginHistoryKey, phone, password, loginOptions?.savePassword ?? true)
    return user
  }

  async function loginWithCarrierToken(carrierToken: string, loginOptions?: CarrierOneClickLoginOptions) {
    const result = await loginByCarrierToken(carrierToken, loginOptions ?? sessionLoginOptions(), httpClient)
    return applyLoginResult(result)
  }

  // 页面刷新或路由守卫进入时使用本地 accessToken 恢复当前用户。
  async function restore() {
    if (state.restoring) {
      return Boolean(state.currentUser)
    }

    const token = getAccessToken()
    if (!token) {
      state.currentUser = null
      return false
    }

    state.restoring = true
    try {
      state.currentUser = await fetchCurrentUser()
      return true
    } catch {
      clear()
      return false
    } finally {
      state.restoring = false
    }
  }

  async function logoutSession() {
    try {
      await logout(httpClient)
    } catch {
      // logout 是 best-effort：后端请求失败也必须清理本地 token，让页面继续跳登录页。
    } finally {
      clear()
    }
  }

  // fileNo 是稳定文件编号，适用于文件已经上传完成，只需要绑定为头像的场景。
  async function updateAvatar(fileNo: string) {
    state.currentUser = await httpClient.post<CurrentUser>(
      `${currentUserApi}/avatar`,
      { fileNo }
    )
    return state.currentUser
  }

  // 浏览器选择本地文件时走完整上传流程：上传文件 -> 获取 fileNo -> 绑定头像。
  async function uploadAvatar(file: File, uploadOptions: Omit<WebFileUploadOptions, 'file' | 'fileCategory'> = {}) {
    const uploadedFile = await uploadWebFile({
      ...uploadOptions,
      file,
      fileCategory: 'IMAGE',
      httpClient,
      scope: fileScope(accessScope)
    })
    return updateAvatar(uploadedFile.fileNo)
  }

  function sessionLoginOptions() {
    return {
      appCode,
      clientType,
      accessScope
    }
  }

  function sessionPasswordLoginOptions(loginOptions?: PhonePasswordLoginOptions): PasswordLoginApiOptions {
    return {
      appCode: loginOptions?.appCode ?? appCode,
      clientType: loginOptions?.clientType ?? clientType,
      accessScope: loginOptions?.accessScope ?? accessScope
    }
  }
}

function requireText(value: string, name: string) {
  const safeValue = value?.trim()
  if (!safeValue) {
    throw new Error(`${name} 不能为空`)
  }
  return safeValue
}

function storageKey(prefix: string | undefined, name: string, fallback: string) {
  const safePrefix = prefix?.trim()
  return safePrefix ? `${safePrefix}.${name}` : fallback
}

// 文件模块也按访问面拆前缀，头像上传需要把 accessScope 转成文件模块的 scope。
function fileScope(accessScope: string): 'client' | 'admin' | 'platform' {
  const normalizedScope = accessScope.trim().toUpperCase()
  if (normalizedScope === 'ADMIN') {
    return 'admin'
  }
  if (normalizedScope === 'PLATFORM') {
    return 'platform'
  }
  return 'client'
}
