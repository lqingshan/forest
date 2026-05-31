import type { MiniappRecord, MiniappRouter, MiniappSessionStore } from '@forest/wechat-miniapp-platform'

/**
 * 这个包是“微信小程序客户端 session 编排层”。
 *
 * 它位于 platform 和具体 app 之间：
 *
 * platform 层：
 * - 只知道 wx.request、wx.login、storage、router 这些微信平台能力
 *
 * client-session 层：
 * - 负责登录流程、恢复登录态、刷新 token、401 清理、登录守卫
 * - 但不直接 import 具体业务用户包，也不直接知道任何业务 API
 *
 * app 层：
 * - 把具体 app 的登录 API、当前用户 API 注入进来
 *
 * 这样未来多个微信小程序可以复用这套 session 编排逻辑，只替换注入函数。
 */

/**
 * 微信登录接口返回的最小 token 结构。
 *
 * 具体 app 的登录结果可以有更多字段，但至少要有 accessToken。
 * refreshToken 是可选的，因为不同系统可能不支持刷新 token。
 */
export interface WechatClientLoginResult {
  accessToken: string
  refreshToken?: string
}

/**
 * 登录上下文由具体 app 决定，例如微信手机号授权 code、渠道来源等。
 *
 * client-session 只负责把上下文透传给 app 注入的登录 API，不理解其中的业务含义。
 */
export type WechatClientLoginContext = Record<string, unknown>

/**
 * 刷新 accessToken 的最小返回结构。
 *
 * 这里不要求返回 refreshToken，是因为当前设计沿用已有 refreshToken：
 * refresh 成功后只替换 accessToken，refreshToken 保持不变。
 */
export interface RefreshAccessTokenResult {
  accessToken: string
}

/**
 * 页面登录守卫的参数。
 *
 * redirect 表示：如果当前没登录，要跳登录页，登录成功后应该回到哪里。
 */
export interface EnsureSessionOptions {
  redirect?: string
}

/**
 * 清理登录态的参数。
 *
 * force 用于“用户主动退出登录”这类强制清理场景。
 * 非 force 清理通常来自 401，要避免旧请求误删新 token。
 */
export interface ClearClientSessionOptions {
  force?: boolean
}

/**
 * 配置 HTTP session 时需要注入给 @forest/http-client 的两个钩子。
 *
 * refreshAccessToken：
 * - HTTP client 收到 401 后可以尝试刷新 token
 *
 * onUnauthorized：
 * - 刷新失败或没有 token 时执行
 * - 通常会清理 session 并跳登录页
 */
export interface ConfigureHttpSessionOptions {
  refreshAccessToken: () => Promise<string | null>
  onUnauthorized: () => Promise<void> | void
}

/**
 * 创建微信小程序 session 门面的所有依赖。
 *
 * 这些依赖全部从外部注入，是这个包保持“通用”的关键：
 *
 * - sessionStore：platform 层提供，只负责 token/user 的存储
 * - router：platform 层提供，但页面路径由 app 配好
 * - configureHttpSession：app/platform 组合后的 HTTP 配置函数
 * - requestLoginCode：通常包装 wx.login
 * - loginByWechat：具体业务登录 API，由 app 注入
 * - refreshAccessToken：具体业务刷新 API
 * - fetchCurrentUser：具体业务“获取当前用户”API
 *
 * 泛型说明：
 * - TUser：当前 app 的 currentUser 类型
 * - TLoginResult：当前 app 的登录结果类型
 */
export interface CreateWechatClientSessionOptions<TUser extends MiniappRecord, TLoginResult extends WechatClientLoginResult> {
  sessionStore: MiniappSessionStore
  router: Pick<MiniappRouter, 'buildCurrentPageUrl' | 'redirectToLogin'>
  configureHttpSession: (options: ConfigureHttpSessionOptions) => void
  requestLoginCode: () => Promise<string>
  loginByWechat: (code: string, context?: WechatClientLoginContext) => Promise<TLoginResult>
  refreshAccessToken: (refreshToken: string) => Promise<RefreshAccessTokenResult>
  fetchCurrentUser: () => Promise<TUser>
}

/**
 * createWechatClientSession 返回给 app/page 使用的 session 门面。
 *
 * 页面层只应该调用这些方法，而不是直接操作 storage、wx.login、
 * HTTP token 注入或业务登录 API。
 */
export interface WechatClientSession<TUser extends MiniappRecord, TLoginResult extends WechatClientLoginResult> {
  /**
   * 配置当前 JS bundle 内的 HTTP client：
   * - baseUrl
   * - wx.request transport
   * - token 注入
   * - 401 refresh/unauthorized hook
   */
  configureClientSession(): void
  /**
   * 从 storage 恢复 token/currentUser 到内存 state。
   * 通常在 App.onLaunch 调用。
   */
  hydrateClientSession(): void
  /**
   * 读取当前缓存用户，不发请求。
   */
  getCachedCurrentUser(): TUser | null
  /**
   * 清理登录态。退出登录时通常传 { force: true }。
   */
  clearClientSession(options?: ClearClientSessionOptions): boolean
  /**
   * 用 refreshToken 换新的 accessToken。
   */
  refreshClientAccessToken(): Promise<string | null>
  /**
   * 用户点击“微信登录”时调用的完整登录链路。
   */
  loginWithWechat(context?: WechatClientLoginContext): Promise<{ loginResult: TLoginResult, currentUser: TUser }>
  /**
   * 冷启动或页面启动时，用已有 accessToken 拉取 currentUser。
   */
  restoreClientSession(): Promise<boolean>
  /**
   * 页面登录守卫：确保当前有可用 session，否则跳登录页。
   */
  ensureClientSession(options?: EnsureSessionOptions): Promise<boolean>
}

/**
 * 创建微信小程序客户端 session 门面。
 *
 * 注意：这是工厂函数。每个 app 会调用它一次，把自己的业务 API 和平台配置注入进来。
 */
export function createWechatClientSession<TUser extends MiniappRecord, TLoginResult extends WechatClientLoginResult>(
  options: CreateWechatClientSessionOptions<TUser, TLoginResult>
): WechatClientSession<TUser, TLoginResult> {
  /**
   * restorePromise 用来合并并发恢复请求。
   *
   * 例如多个页面/组件几乎同时调用 restoreClientSession()，
   * 我们只希望真正请求一次 fetchCurrentUser，其它调用复用同一个 Promise。ÏÏ
   */
  let restorePromise: Promise<boolean> | null = null
  /**
   * 小程序 esbuild 多入口 bundle 下，app.js 和 page.js 可能各有一份模块状态。
   * 在“当前 bundle 内”，HTTP session 只需要配置一次，所以用这个 guard。
   */
  let httpSessionConfigured = false

  function configureClientSession() {
    if (httpSessionConfigured) {
      return
    }

    /**
     * 这里把 session 的刷新/401 策略交给 HTTP client。
     *
     * HTTP client 不知道小程序路由，也不知道 token 存在哪里；
     * 它只知道：
     * - 401 时先调用 refreshAccessToken
     * - 如果刷新失败，再调用 onUnauthorized
     */
    options.configureHttpSession({
      refreshAccessToken: refreshClientAccessToken,
      onUnauthorized: async () => {
        /**
         * clearClientSession() 返回是否真的清掉了持久化 storage。
         *
         * 如果返回 false，说明 storage 里已经有更新的 token；
         * 这种情况通常是旧请求的 401，不应该打断新登录态。
         */
        const cleared = clearClientSession()
        if (cleared) {
          /**
           * 确认当前有效 session 被清理后，才跳登录页。
           * redirect 记录当前页面，登录成功后可回到原页面。
           */
          options.router.redirectToLogin({
            redirect: options.router.buildCurrentPageUrl()
          })
        }
      }
    })
    httpSessionConfigured = true
  }

  function hydrateClientSession() {
    /**
     * hydrate 也先配置 HTTP。
     *
     * 这是为了避免页面 bundle 没执行 App.onLaunch 里的配置时，
     * 自己发请求仍落到默认 HTTP adapter。
     */
    configureClientSession()
    options.sessionStore.hydrateSessionStore()
  }

  function getCachedCurrentUser() {
    /**
     * 只读缓存，不主动发请求。
     * 页面可以用它决定是否先展示缓存用户。
     */
    return options.sessionStore.getCachedUser<TUser>()
  }

  function clearClientSession(clearOptions: ClearClientSessionOptions = {}) {
    /**
     * 清理 session 后，正在进行中的恢复请求不应再代表当前登录态。
     */
    restorePromise = null
    return options.sessionStore.clearSessionStore(clearOptions)
  }

  async function refreshClientAccessToken() {
    /**
     * 这里也要幂等配置 HTTP。
     *
     * 原因和 loginWithWechat 类似：小程序多入口 bundle 可能导致某个页面入口
     * 没有继承 app.js 那份 clientHttp 配置。
     */
    configureClientSession()
    options.sessionStore.ensureSessionStoreHydrated()
    const refreshToken = options.sessionStore.getRefreshToken()
    if (!refreshToken) {
      // 没有 refreshToken 就无法刷新，交给 HTTP client 进入 unauthorized 流程。
      return null
    }

    // 具体刷新接口由 app 注入。这个通用包不关心后端 URL。
    const result = await options.refreshAccessToken(refreshToken)
    // 当前策略：保存新的 accessToken，沿用旧 refreshToken。
    options.sessionStore.saveSessionTokens(result.accessToken, refreshToken)
    return options.sessionStore.getAccessToken()
  }

  async function loginWithWechat(context?: WechatClientLoginContext) {
    /**
     * 用户主动点击“微信登录”时的完整链路：
     *
     * 1. 确保当前 bundle 的 HTTP client 已配置为 wx.request
     * 2. 从 wx.login 获取一次性 code
     * 3. 把 code 交给后端，换业务 token
     * 4. 保存 token
     * 5. 用 token 请求 currentUser
     * 6. 确认过程中登录态没有被其它流程改掉
     * 7. 缓存 currentUser
     */
    configureClientSession()
    options.sessionStore.ensureSessionStoreHydrated()
    const loginCode = await options.requestLoginCode()
    const loginResult = await options.loginByWechat(loginCode, context)
    options.sessionStore.saveSessionTokens(loginResult.accessToken, loginResult.refreshToken)
    /**
     * 记录登录后的 revision/token。
     *
     * fetchCurrentUser 是异步请求。在它返回前，用户可能退出登录、
     * 重新登录，或另一个请求刷新了 token。
     */
    const loginRevision = options.sessionStore.getSessionRevision()
    const loginAccessToken = options.sessionStore.getAccessToken()
    const currentUser = await options.fetchCurrentUser()
    if (
      /**
       * 三重校验：
       * - revision 没变：session 没被清理/替换
       * - accessToken 没变：当前内存 token 还是登录时那个
       * - persisted token 匹配：storage 中也还是同一个 token
       *
       * 任何一个不满足，都说明这个 currentUser 是旧登录态的结果，
       * 不能保存，否则会出现“旧用户覆盖新用户”的问题。
       */
      options.sessionStore.getSessionRevision() !== loginRevision ||
      options.sessionStore.getAccessToken() !== loginAccessToken ||
      !options.sessionStore.isPersistedAccessToken(loginAccessToken)
    ) {
      throw new Error('登录状态已变化，请重新登录')
    }
    // 再次通过 token 匹配保护后，才把 currentUser 写入缓存。
    options.sessionStore.saveCachedUserIfTokenMatches(currentUser, loginAccessToken)
    return {
      loginResult,
      currentUser
    }
  }

  async function restoreClientSession() {
    /**
     * restore = 已经有 accessToken 时，后台拉取 currentUser，恢复完整登录态。
     *
     * 它和 loginWithWechat 的区别：
     * - 不调用 wx.login
     * - 不调用登录接口
     * - 只用已有 token 请求 currentUser
     */
    configureClientSession()
    options.sessionStore.ensureSessionStoreHydrated()
    if (restorePromise) {
      // 合并并发 restore，避免多个页面同时打 currentUser 接口。
      return restorePromise
    }

    const accessToken = options.sessionStore.getAccessToken()
    if (!accessToken) {
      // 没 token 就没有可恢复的 session。
      return false
    }

    // 记录 restore 开始时的 session 版本，后面防止旧请求覆盖新状态。
    const restoreRevision = options.sessionStore.getSessionRevision()
    restorePromise = options.fetchCurrentUser()
      .then((user) => {
        if (
          /**
           * currentUser 请求成功，也不能马上保存。
           *
           * 需要确认：
           * - session revision 没变
           * - 内存 accessToken 没变
           * - storage 里的 accessToken 也还是同一个
           */
          options.sessionStore.getSessionRevision() !== restoreRevision ||
          options.sessionStore.getAccessToken() !== accessToken ||
          !options.sessionStore.isPersistedAccessToken(accessToken)
        ) {
          // session 已变化，忽略这次 restore 结果。
          return false
        }
        return options.sessionStore.saveCachedUserIfTokenMatches(user, accessToken)
      })
      .catch(() => {
        /**
         * fetchCurrentUser 失败通常表示 token 已失效或网络异常。
         *
         * 只有当 session 仍然是 restore 开始时那一份，才清理；
         * 如果期间已经有新 token，就不能被这个旧失败覆盖。
         */
        if (
          options.sessionStore.getSessionRevision() === restoreRevision &&
          options.sessionStore.getAccessToken() === accessToken &&
          options.sessionStore.isPersistedAccessToken(accessToken)
        ) {
          clearClientSession()
        }
        return false
      })
      .finally(() => {
        // restore 结束后允许下一次调用重新发起恢复。
        restorePromise = null
      })

    return restorePromise
  }

  async function ensureClientSession(ensureOptions: EnsureSessionOptions = {}) {
    /**
     * ensure = 页面登录守卫。
     *
     * 受保护页面 onLoad/onShow 时调用它：
     * - 有 currentUser + accessToken：直接通过
     * - 没 accessToken：跳登录页
     * - 有 accessToken 但还没 currentUser：先放行，并后台 restore
     */
    configureClientSession()
    options.sessionStore.ensureSessionStoreHydrated()
    const currentUser = getCachedCurrentUser()
    const accessToken = options.sessionStore.getAccessToken()
    if (currentUser && accessToken) {
      return true
    }

    if (currentUser && !accessToken) {
      // 有用户缓存但没有 token，是不一致状态；清理掉，避免页面误以为已登录。
      clearClientSession()
    }

    if (!options.sessionStore.getAccessToken()) {
      // 无 token 才跳登录。redirect 由页面传入，通常是当前页面 URL。
      options.router.redirectToLogin({
        redirect: ensureOptions.redirect
      })
      return false
    }

    if (!getCachedCurrentUser()) {
      /**
       * 有 token 但没有 currentUser，说明可能是冷启动刚恢复 token。
       *
       * 这里不阻塞页面，也不主动跳登录；后台补 currentUser。
       * 如果 token 真的无效，页面自己的业务请求或 HTTP 401 流程会处理。
       */
      restoreClientSession().catch(() => {
        // 页面自己的接口会处理 401；这里不因为后台补用户失败抢路由。
      })
    }

    return true
  }

  return {
    /**
     * 返回稳定门面，让 app/page 只依赖这些函数。
     * 具体实现细节仍封装在 createWechatClientSession 内部闭包里。
     */
    configureClientSession,
    hydrateClientSession,
    getCachedCurrentUser,
    clearClientSession,
    refreshClientAccessToken,
    loginWithWechat,
    restoreClientSession,
    ensureClientSession
  }
}
