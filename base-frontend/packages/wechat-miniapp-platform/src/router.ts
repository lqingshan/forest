import { getRuntime, type MiniappRecord, type MiniappRuntimeOptions } from './runtime'

/**
 * 小程序路由适配。
 *
 * 先把“小程序页面栈”想成一摞卡片：
 * - 用户每打开一个新页面，微信就可能往这摞卡片最上面放一张新卡片。
 * - 最上面的卡片就是“当前页”，它下面那张就是用户点返回时会看到的页面。
 * - 如果这摞卡片一直增加，用户按返回会经过很多旧页面，体验会很绕。
 *
 * 微信提供了几种常见路由动作：
 * - wx.redirectTo：替换当前卡片，只关掉当前页，再打开目标页。
 * - wx.reLaunch：清空整摞卡片，再打开目标页，适合重新进入一个确定入口。
 * - wx.navigateBack：拿掉当前卡片，回到上一张卡片。
 *
 * 这个模块只封装 wx.reLaunch、wx.redirectTo、wx.navigateBack 和页面栈读取。
 * 页面路径由具体 app 注入，公共 platform 包不写死业务页面。
 */

/**
 * Router 不写死业务页面。
 *
 * 每个小程序 app 自己告诉平台层：
 * - 登录页是哪一个
 * - 默认首页是哪一个
 * - 哪些页面是一层主页面
 */
export interface CreateMiniappRouterOptions extends MiniappRuntimeOptions {
  /**
   * 登录页路径。
   *
   * 当页面发现用户没有登录态时，会跳到这个页面。
   * 例如 trade-leads 小程序里是 /pages/login/index。
   */
  loginPage: string

  /**
   * 默认兜底页路径。
   *
   * 当我们不知道该跳哪里时，就回到这个页面。
   * 例如没有上一页、redirect 参数为空、页面栈异常时，都可以用它兜底。
   */
  defaultPage: string

  /**
   * 业务一级入口页列表。
   *
   * 这里的 primary page 不是微信原生 tabBar 的意思，而是业务上“可以作为入口”的页面。
   * 例如线索池、已解锁、我的。打开这些页面时，我们通常不想保留旧 query。
   */
  primaryPages: string[]
}

/**
 * 路由 API 的可选回调。
 *
 * 微信小程序的 wx.redirectTo / wx.reLaunch / wx.navigateBack 不是 Promise。
 * 如果跳转失败，它不会自动 throw，而是调用 fail 回调。
 * 因此页面想排查“点了没反应”时，就需要把 fail 接出来看 errMsg。
 */
export interface MiniappRouteCallbacks {
  /**
   * 路由调用成功后的回调。
   *
   * 当前项目暂时不依赖成功回调做业务逻辑，但保留它方便调试或后续扩展。
   */
  success?: () => void

  /**
   * 路由调用失败后的回调。
   *
   * error 通常会带 errMsg，例如路径不合法、不能跳转到目标页等微信侧原因。
   */
  fail?: (error: MiniappRecord) => void
}

/**
 * 小程序路由门面。
 *
 * 页面层使用这些方法时，不需要反复判断：
 * - 当前有没有页面栈
 * - 应该 redirectTo 还是 reLaunch
 * - 登录页 redirect 参数怎么拼
 */
export interface MiniappRouter {
  /**
   * 去掉 URL 上的 query，只保留页面路径。
   *
   * 例如 /pages/leads/index?keyword=phone 会变成 /pages/leads/index。
   * 这样可以判断“是不是同一个页面”，而不被 keyword、id 这类参数干扰。
   */
  stripQuery(url: string): string

  /**
   * 替换当前页面。
   *
   * 如果页面栈里有上一页，就用 wx.redirectTo 替换当前页。
   * 如果页面栈为空或只有当前页，就用 wx.reLaunch 兜底，确保目标页一定能打开。
   */
  replacePage(url: string, callbacks?: MiniappRouteCallbacks): void

  /**
   * 清空页面栈后打开目标页。
   *
   * 适合支付结果页这类“流程终点”：支付完成后再点回到我的、继续充值、返回线索池，
   * 应该重新进入目标页面，而不是保留旧的充值页/支付页在返回栈里。
   */
  reLaunchPage(url: string, callbacks?: MiniappRouteCallbacks): void

  /**
   * 给页面 URL 拼 query 参数。
   *
   * 它会过滤 undefined、null、空字符串，并自动做 encodeURIComponent。
   * 例如 appendQuery('/pages/detail/index', { id: 3 }) 得到 /pages/detail/index?id=3。
   */
  appendQuery(url: string, params: Record<string, string | number | boolean | null | undefined>): string

  /**
   * 从微信页面栈里还原当前页面 URL。
   *
   * 登录守卫会用它记录“用户原本在哪个页面”。
   * 这样跳到登录页后，登录成功还能回到原页面。
   */
  buildCurrentPageUrl(): string

  /**
   * 跳到登录页。
   *
   * redirect 表示“登录成功后要回到哪里”。
   * 例如用户在详情页没登录，就跳到登录页，并把详情页地址放进 redirect。
   */
  redirectToLogin(options?: { redirect?: string }): void

  /**
   * 打开业务一级入口页。
   *
   * 如果目标是 primaryPages 里的页面，会先去掉 query，再打开基础路径。
   * 这样线索池、我的这类入口页不会被旧参数污染状态。
   */
  openPrimaryPage(url: string, callbacks?: MiniappRouteCallbacks): void

  /**
   * 有上一页就返回上一页，没有上一页就打开兜底页。
   *
   * 适合“返回”按钮：用户从列表进详情就自然返回列表；
   * 用户从分享或冷启动直接进详情时，没有上一页，就回到首页。
   */
  goBackOr(url: string, callbacks?: MiniappRouteCallbacks): void
}

/**
 * 创建小程序路由工具。
 *
 * 为什么 router 放在 platform 层？
 * - wx.reLaunch / wx.redirectTo / wx.navigateBack 是微信平台 API。
 * - 但 loginPage/defaultPage/primaryPages 是 app 自己的配置。
 *
 * 所以这里通过 options 注入页面路径，避免公共包写死 trade-leads 的页面。
 */
export function createMiniappRouter(options: CreateMiniappRouterOptions): MiniappRouter {
  // primaryPages 只关心“页面路径”，不关心 query。
  // 例如 /pages/me/index?tab=profile 和 /pages/me/index 都应该算同一个一级入口页。
  const primaryPages = new Set(options.primaryPages.map(stripQuery))

  function replacePage(url: string, callbacks?: MiniappRouteCallbacks) {
    const runtime = getRuntime(options.runtime)
    const pages = runtime.getCurrentPages?.() || []
    if (!pages.length || pages.length <= 1) {
      // 页面栈为空或只有 1 页时，redirectTo 语义上“替换当前页”不够稳。
      // 这时用 reLaunch 清空并打开目标页，等于重新进入一个确定入口。
      // 典型场景：用户从分享、扫码、冷启动直接进入某个页面。
      runtime.wx.reLaunch(createRoutePayload('reLaunch', url, callbacks))
      return
    }
    // 页面栈里有上一页时，用 redirectTo 替换当前页。
    // 这样不会新增一页，也不会让用户后退时回到刚刚被替换掉的旧页面。
    // 例如：列表 -> 详情 -> 登录，登录成功后 replace 到详情，而不是再压一张详情。
    runtime.wx.redirectTo(createRoutePayload('redirectTo', url, callbacks))
  }

  function reLaunchPage(url: string, callbacks?: MiniappRouteCallbacks) {
    getRuntime(options.runtime).wx.reLaunch(createRoutePayload('reLaunch', url, callbacks))
  }

  function appendQuery(url: string, params: Record<string, string | number | boolean | null | undefined>) {
    // null/undefined/空字符串都不拼进 query，避免出现 keyword= 这种无意义参数。
    const entries = Object.entries(params || {}).filter(([, value]) => value !== undefined && value !== null && value !== '')
    if (!entries.length) {
      return url
    }
    const query = entries
      .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
      .join('&')
    return `${url}${url.includes('?') ? '&' : '?'}${query}`
  }

  function buildCurrentPageUrl() {
    const pages = getRuntime(options.runtime).getCurrentPages?.() || []
    if (!pages.length) {
      // 某些启动早期场景可能拿不到页面栈，此时退回 app 默认页。
      return options.defaultPage
    }
    const current = pages[pages.length - 1]
    return appendQuery(`/${current.route}`, current.options || {})
  }

  function openPrimaryPage(url: string, callbacks?: MiniappRouteCallbacks) {
    const normalized = stripQuery(url)
    if (primaryPages.has(normalized)) {
      // 入口页只按基准路径打开，不带旧 query。
      // 举例：/pages/leads/index?keyword=phone 是一次搜索状态；
      // 再次回到线索池入口时，通常希望是干净的 /pages/leads/index。
      replacePage(normalized, callbacks)
      return
    }
    // 不是一级入口页时保留完整 URL。
    // 详情页这类页面需要 id，例如 /pages/lead-detail/index?id=123，不能丢 query。
    replacePage(url, callbacks)
  }

  return {
    stripQuery,
    replacePage,
    reLaunchPage,
    appendQuery,
    buildCurrentPageUrl,
    redirectToLogin(loginOptions: { redirect?: string } = {}) {
      const currentUrl = stripQuery(buildCurrentPageUrl())
      if (currentUrl === options.loginPage) {
        // 已经在登录页时不再跳登录，避免 login -> login 循环。
        // 如果没有这个保护，登录页自己发现“还没登录”时可能又跳回登录页。
        return
      }

      // redirect 通过 query 带给登录页。
      // 登录页完成登录后，就可以按 redirect 把用户送回原来的页面。
      replacePage(appendQuery(options.loginPage, {
        redirect: loginOptions.redirect || options.defaultPage
      }))
    },
    openPrimaryPage,
    goBackOr(url: string, callbacks?: MiniappRouteCallbacks) {
      const runtime = getRuntime(options.runtime)
      const pages = runtime.getCurrentPages?.() || []
      if (pages.length > 1) {
        // 有上一页就返回上一页，保持用户自然导航体验。
        // 例如：线索池 -> 线索详情，详情页点返回就应该回线索池。
        runtime.wx.navigateBack(createRoutePayload('navigateBack', url, callbacks))
        return
      }
      // 没有上一页时打开兜底页。
      // 例如用户从分享卡片直接进入详情页，页面栈里没有“线索池”可返回，
      // 这时就打开传入的 url；如果 url 也为空，再回 defaultPage。
      openPrimaryPage(url || options.defaultPage, callbacks)
    }
  }
}

function createRoutePayload(action: string, url: string, callbacks?: MiniappRouteCallbacks) {
  return {
    ...(action === 'navigateBack' ? {} : { url }),
    success: callbacks?.success,
    fail(error: MiniappRecord) {
      console.error('[miniapp-router] route failed', { action, url, error })
      callbacks?.fail?.(error)
    }
  }
}

/**
 * 去掉 URL query，只保留页面路径。
 *
 * 常用于：
 * - 判断当前是不是登录页
 * - 判断某个 url 是否属于 primary page
 */
function stripQuery(url: string) {
  return (url || '').split('?')[0]
}
