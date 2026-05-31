import { fetchCurrentUser } from './api'
import type { CurrentUser } from './types'
import { CURRENT_USER_COPY, getCurrentUserProfileErrorMessage } from './view-model'

declare const wx: {
  showModal(options: {
    title: string
    content: string
    success(result: { confirm: boolean }): void
  }): void
  stopPullDownRefresh(): void
}

interface CurrentUserMiniappAuth {
  ensureClientSession(options?: { redirect?: string }): Promise<boolean>
  getCachedCurrentUser(): CurrentUser | null
  clearClientSession(options?: { force?: boolean }): boolean
}

interface CurrentUserMiniappRouter {
  openPrimaryPage(url: string): void
}

interface CurrentUserPageContext {
  data: Record<string, unknown>
  setData(patch: Record<string, unknown>): void
  loadProfile(): Promise<void>
}

export interface CurrentUserPageShowHandlerOptions {
  auth: Pick<CurrentUserMiniappAuth, 'ensureClientSession'>
  redirect: string
}

export interface CurrentUserProfileLoaderOptions {
  auth?: Pick<CurrentUserMiniappAuth, 'getCachedCurrentUser'>
  fetchCurrentUser?: () => Promise<CurrentUser>
  loadExtras?: () => Promise<Record<string, unknown>>
}

export interface CurrentUserLogoutHandlerOptions {
  auth: Pick<CurrentUserMiniappAuth, 'clearClientSession'>
  router: CurrentUserMiniappRouter
  loginPath: string
  resetData?: Record<string, unknown>
}

/**
 * 创建微信小程序“我的页”进入时的登录守卫。
 *
 * <p>登录态检查属于用户基础能力；具体页面地址由 app 注入，避免 user 模块写死业务路由。</p>
 */
export function createCurrentUserPageShowHandler(options: CurrentUserPageShowHandlerOptions) {
  return async function handleCurrentUserPageShow(this: CurrentUserPageContext) {
    const ready = await options.auth.ensureClientSession({
      redirect: options.redirect
    })
    if (!ready) {
      return
    }
    await this.loadProfile()
  }
}

/**
 * 创建微信小程序“我的页”下拉刷新处理器。
 */
export function createCurrentUserPullDownRefreshHandler() {
  return function handleCurrentUserPullDownRefresh(this: CurrentUserPageContext) {
    this.loadProfile().finally(() => {
      wx.stopPullDownRefresh()
    })
  }
}

/**
 * 创建微信小程序当前用户资料加载器。
 *
 * <p>默认加载 CLIENT 访问面的 {@code fetchCurrentUser()}；如果 app 是 ADMIN / PLATFORM
 * 小程序，应该注入对应访问面的 {@code fetchCurrentUser}。如果 app 需要在“我的页”组合积分、会员、
 * 订单数量等数据，可以通过 {@code loadExtras} 注入，user 模块只负责并行编排和状态维护。</p>
 */
export function createCurrentUserProfileLoader(options: CurrentUserProfileLoaderOptions = {}) {
  return async function loadCurrentUserProfile(this: CurrentUserPageContext) {
    this.setData({
      loading: true,
      errorMessage: ''
    })

    try {
      const fetcher = options.fetchCurrentUser || fetchCurrentUser
      const [user, extras] = await Promise.all([
        fetcher(),
        options.loadExtras ? options.loadExtras() : Promise.resolve({})
      ])
      this.setData({
        user: user || options.auth?.getCachedCurrentUser() || null,
        ...extras
      })
    } catch (error) {
      this.setData({
        errorMessage: getCurrentUserProfileErrorMessage(error)
      })
    } finally {
      this.setData({
        loading: false
      })
    }
  }
}

/**
 * 创建微信小程序当前用户退出登录处理器。
 *
 * <p>退出登录的确认弹窗、清理本地 session 和跳转登录页是通用流程；
 * app 可以通过 {@code resetData} 补充清理自己的页面状态，比如积分余额。</p>
 */
export function createCurrentUserLogoutHandler(options: CurrentUserLogoutHandlerOptions) {
  return function handleCurrentUserLogout(this: CurrentUserPageContext) {
    wx.showModal({
      title: CURRENT_USER_COPY.logoutDialog.title,
      content: CURRENT_USER_COPY.logoutDialog.content,
      success: (result: { confirm: boolean }) => {
        if (!result.confirm) {
          return
        }
        options.auth.clearClientSession({ force: true })
        this.setData({
          user: null,
          errorMessage: '',
          ...options.resetData
        })
        options.router.openPrimaryPage(options.loginPath)
      }
    })
  }
}
