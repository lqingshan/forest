import { getWechatPhoneLoginErrorMessage, WECHAT_LOGIN_COPY, WECHAT_PHONE_LOGIN_COPY } from '../view-model'

/**
 * 微信小程序手机号授权登录 flow。
 *
 * 这个文件不是“小程序所有登录方式”的通用入口，而是专门服务这条链路：
 *
 * wechat-phone-login-panel
 * -> 用户点击 open-type="getPhoneNumber" 按钮
 * -> 组件拿到 event.detail.code，并向页面 emit phonelogin({ phoneCode })
 * -> 页面把事件交给本 flow
 * -> 本 flow 调用 app 注入的 auth.loginWithWechat({ phoneCode })
 * -> 小程序 session 层再执行 wx.login()，把 wx.login code + phoneCode 交给后端
 * -> 后端解析 openId 和手机号，创建/复用账号并签发 token
 *
 * 这样拆分后：
 * - 组件只关心微信授权按钮和事件解析。
 * - flow 只关心页面级交互编排。
 * - app 只负责注入 auth/router/defaultRedirect/primaryPages。
 * - 后端登录接口、token 保存、currentUser 恢复仍由更底层 session 能力负责。
 */

/**
 * 当前文件只使用微信 UI API，不直接使用 wx.login / wx.request。
 *
 * wx.login / wx.request 已被更底层的小程序平台包和 session 编排包封装。
 * 这里声明最小 wx 类型，是为了让 user 模块保持低耦合，不引入完整微信类型包。
 */
declare const wx: {
  showLoading(options: { title: string }): void
  hideLoading(): void
  showToast(options: { title: string; icon: 'success' | 'none' }): void
}

/**
 * app 注入的微信登录门面。
 *
 * 方法名仍叫 loginWithWechat，是因为底层 session 门面统一表达“微信小程序登录”。
 * 在本 flow 中会强制传入 { phoneCode }，所以实际请求会走后端
 * /api/auth/wechat-miniapp/phone-login，而不是微信直接登录接口。
 */
export interface WechatMiniappPhoneLoginAuth {
  loginWithWechat(context?: Record<string, unknown>): Promise<unknown>
}

/**
 * app 注入的小程序路由门面。
 *
 * flow 不直接调用 wx.navigateTo / wx.redirectTo，因为不同 app 对一级 tab 页、
 * 普通页面、登录后回跳的处理策略可能不同。
 */
export interface WechatMiniappPhoneLoginRouter {
  openPrimaryPage(url: string): void
  replacePage(url: string): void
}

/**
 * 微信小程序 Page 实例需要具备的最小形状。
 *
 * 这里不依赖具体 app 页面类型，只要求页面 data 里有 submitting/redirect，
 * 并且能通过 setData 更新 submitting/errorMessage。
 */
export interface WechatMiniappPhoneLoginPageContext {
  data: {
    /**
     * 防重复提交开关。
     *
     * 手机号授权按钮可能被用户连续点击，或者微信回调重复触发；
     * flow 通过这个字段保证同一时刻只跑一条登录链路。
     */
    submitting?: boolean
    /**
     * 登录成功后回跳地址。
     *
     * 通常由登录页 onLoad 从 query.redirect 解析出来。
     * 如果不存在，则使用 options.defaultRedirect。
     */
    redirect?: string
  }
  setData(patch: {
    submitting?: boolean
    errorMessage?: string
  }): void
}

/**
 * 创建手机号授权登录处理器所需的 app 注入项。
 */
export interface WechatMiniappPhoneLoginHandlerOptions {
  /** app 已装配好的认证门面。 */
  auth: WechatMiniappPhoneLoginAuth
  /** app 已装配好的路由门面。 */
  router: WechatMiniappPhoneLoginRouter
  /** 没有 redirect 时的默认落点。 */
  defaultRedirect: string
  /**
   * app 的一级主页面列表。
   *
   * 登录成功后如果 redirect 指向一级页面，应该走 openPrimaryPage；
   * 否则走 replacePage，避免普通详情页被当成一级入口处理。
   */
  primaryPages: string[]
  /** 可选覆盖 loading 文案；默认使用 WECHAT_LOGIN_COPY.loadingText。 */
  loadingText?: string
  /** 可选覆盖登录成功 toast 文案；默认使用 WECHAT_LOGIN_COPY.successText。 */
  successText?: string
}

/**
 * 创建手机号授权失败处理器所需的配置。
 */
export interface WechatMiniappPhoneErrorHandlerOptions {
  /** 组件没有传 message 时使用的默认错误文案。 */
  defaultMessage?: string
}

/**
 * 创建微信小程序手机号授权登录处理器。
 *
 * <p>组件只负责拿到微信 getPhoneNumber 的 phoneCode；这个 flow 负责页面级编排：
 * 防重复提交、loading、调用登录门面、成功提示、失败文案和 redirect 跳转。</p>
 */
export function createWechatMiniappPhoneLoginHandler(options: WechatMiniappPhoneLoginHandlerOptions) {
  return async function handleWechatMiniappPhoneLogin(this: WechatMiniappPhoneLoginPageContext, event: MiniappRecord) {
    // 已有登录请求进行中时直接忽略，避免重复创建 session 或重复跳转。
    if (this.data.submitting) {
      return
    }

    /**
     * phonelogin 事件来自 wechat-phone-login-panel。
     *
     * 该组件已把微信原始 event.detail.code 转成 phoneCode；
     * 这里再做一次兜底校验，防止页面被其它来源手动触发。
     */
    const phoneCode = resolvePhoneCode(event)
    if (!phoneCode) {
      showPhoneLoginError(this, WECHAT_PHONE_LOGIN_COPY.authorizationRequiredText)
      return
    }

    // 开始登录前清理旧错误，并让按钮进入 loading 态。
    this.setData({
      submitting: true,
      errorMessage: ''
    })

    // 使用微信原生 loading，避免登录请求期间页面看起来没有响应。
    wx.showLoading({
      title: options.loadingText || WECHAT_LOGIN_COPY.loadingText
    })

    /**
     * 记录 loading 是否仍由当前函数持有。
     *
     * 成功分支会先关闭 loading 再 toast/跳转；finally 里只在尚未关闭时兜底关闭，
     * 避免重复 hideLoading 造成小程序端表现不稳定。
     */
    let loadingVisible = true
    try {
      /**
       * 这里虽然调用 loginWithWechat，但因为传入了 phoneCode，
       * app-definition 注入的 createWechatMiniappPhoneAuthApi 会请求
       * /api/auth/wechat-miniapp/phone-login。
       */
      await options.auth.loginWithWechat({ phoneCode })

      wx.hideLoading()
      loadingVisible = false

      // 登录成功给出短反馈，然后根据 redirect 回到目标页面。
      wx.showToast({
        title: options.successText || WECHAT_LOGIN_COPY.successText,
        icon: 'success'
      })

      openRedirect(this, options)
    } catch (error) {
      // 后端错误、网络错误、微信手机号解析失败，都统一转成用户可读文案。
      showPhoneLoginError(this, getWechatPhoneLoginErrorMessage(error))
    } finally {
      if (loadingVisible) {
        wx.hideLoading()
      }
      // 无论成功失败都恢复按钮状态；成功场景页面通常马上跳转，但这里仍保持状态完整。
      this.setData({
        submitting: false
      })
    }
  }
}

/**
 * 创建微信小程序手机号授权失败处理器。
 *
 * <p>用于接收 wechat-phone-login-panel 的 phoneerror 事件，
 * 例如用户拒绝授权或微信没有返回手机号授权 code。</p>
 */
export function createWechatMiniappPhoneErrorHandler(options: WechatMiniappPhoneErrorHandlerOptions = {}) {
  return function handleWechatMiniappPhoneError(this: WechatMiniappPhoneLoginPageContext, event: MiniappRecord) {
    /**
     * phoneerror 事件也来自 wechat-phone-login-panel。
     *
     * 常见原因：
     * - 用户拒绝手机号授权。
     * - 微信没有返回 event.detail.code。
     * - 低版本基础库或配置问题导致 getPhoneNumber 不可用。
     */
    const message = typeof event.detail?.message === 'string' && event.detail.message.trim()
      ? event.detail.message.trim()
      : options.defaultMessage || WECHAT_PHONE_LOGIN_COPY.authorizationRequiredText
    showPhoneLoginError(this, message)
  }
}

/**
 * 从组件事件中解析 phoneCode。
 *
 * 注意这里拿的不是手机号明文，而是微信返回的一次性手机号授权 code。
 * 真实手机号只能由后端调用微信 getuserphonenumber 接口解析。
 */
function resolvePhoneCode(event: MiniappRecord) {
  const phoneCode = typeof event.detail?.phoneCode === 'string' ? event.detail.phoneCode.trim() : ''
  return phoneCode || null
}

/**
 * 统一写入页面错误文案。
 */
function showPhoneLoginError(context: WechatMiniappPhoneLoginPageContext, message: string) {
  context.setData({
    errorMessage: message
  })
}

/**
 * 登录成功后的回跳逻辑。
 *
 * 一级主页面使用 openPrimaryPage，让 app 用自己的主页面打开策略处理；
 * 其它页面使用 replacePage，通常用于登录后回到详情页、结果页等普通页面。
 */
function openRedirect(context: WechatMiniappPhoneLoginPageContext, options: WechatMiniappPhoneLoginHandlerOptions) {
  const redirect = context.data.redirect || options.defaultRedirect
  if (options.primaryPages.some((page) => redirect.startsWith(page))) {
    options.router.openPrimaryPage(redirect)
    return
  }
  options.router.replacePage(redirect)
}
