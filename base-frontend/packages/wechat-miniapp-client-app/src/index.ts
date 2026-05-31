import {
  createWechatClientSession,
  type ConfigureHttpSessionOptions,
  type RefreshAccessTokenResult,
  type WechatClientLoginContext,
  type WechatClientLoginResult,
  type WechatClientSession
} from '@forest/wechat-miniapp-client-session'
import {
  configureMiniappHttpSession,
  createMiniappRouter,
  createSessionStore,
  requestWechatLoginCode,
  requestWechatMiniappPayment,
  type MiniappRecord,
  type MiniappRouter,
  type MiniappRuntimeOptions,
  type WechatMiniappPaymentParams
} from '@forest/wechat-miniapp-platform'

/**
 * 微信小程序 app 装配工厂。
 *
 * 它把：
 * - platform：router / sessionStore / wx.login / wx.requestPayment
 * - client-session：登录、恢复、刷新、401 清理
 * - app definition：页面路径、storage 前缀、业务登录 API
 *
 * 三者装成一个 app facade，让具体小程序客户端只保留自己的定义文件。
 */

export interface CreateWechatMiniappClientAppOptions<
  TUser extends MiniappRecord,
  TLoginResult extends WechatClientLoginResult
> extends MiniappRuntimeOptions {
  storagePrefix: string
  apiBaseUrl: string
  accessScope: string
  loginPage: string
  defaultPage: string
  primaryPages: string[]
  currentUserGlobalDataKey?: string
  loginByWechat: (code: string, context?: WechatClientLoginContext) => Promise<TLoginResult>
  refreshAccessToken: (refreshToken: string) => Promise<RefreshAccessTokenResult>
  fetchCurrentUser: () => Promise<TUser>
}

export interface WechatMiniappClientApp<
  TUser extends MiniappRecord,
  TLoginResult extends WechatClientLoginResult
> {
  lifecycle: Pick<WechatClientSession<TUser, TLoginResult>, 'configureClientSession' | 'hydrateClientSession' | 'restoreClientSession'>
  auth: Pick<
    WechatClientSession<TUser, TLoginResult>,
    'loginWithWechat' | 'ensureClientSession' | 'clearClientSession' | 'getCachedCurrentUser' | 'refreshClientAccessToken'
  >
  router: Pick<MiniappRouter, 'appendQuery' | 'replacePage' | 'reLaunchPage' | 'openPrimaryPage' | 'goBackOr' | 'redirectToLogin' | 'buildCurrentPageUrl'>
  payment: {
    requestWechatMiniappPayment(params: WechatMiniappPaymentParams): Promise<void>
  }
  platform: {
    requestWechatLoginCode(): Promise<string>
  }
}

export function createWechatMiniappClientApp<
  TUser extends MiniappRecord,
  TLoginResult extends WechatClientLoginResult
>(options: CreateWechatMiniappClientAppOptions<TUser, TLoginResult>): WechatMiniappClientApp<TUser, TLoginResult> {
  const sessionStore = createSessionStore({
    storagePrefix: options.storagePrefix,
    currentUserGlobalDataKey: options.currentUserGlobalDataKey,
    runtime: options.runtime
  })
  const router = createMiniappRouter({
    loginPage: options.loginPage,
    defaultPage: options.defaultPage,
    primaryPages: options.primaryPages,
    runtime: options.runtime
  })

  function configureHttpSession(httpOptions: ConfigureHttpSessionOptions) {
    configureMiniappHttpSession({
      accessScope: options.accessScope,
      apiBaseUrl: options.apiBaseUrl,
      sessionStore,
      refreshAccessToken: httpOptions.refreshAccessToken,
      onUnauthorized: httpOptions.onUnauthorized,
      runtime: options.runtime
    })
  }

  function requestLoginCode() {
    return requestWechatLoginCode({
      runtime: options.runtime
    })
  }

  function requestMiniappPayment(params: WechatMiniappPaymentParams) {
    return requestWechatMiniappPayment(params, {
      runtime: options.runtime
    })
  }

  const clientSession = createWechatClientSession<TUser, TLoginResult>({
    sessionStore,
    router,
    configureHttpSession,
    requestLoginCode,
    loginByWechat: options.loginByWechat,
    refreshAccessToken: options.refreshAccessToken,
    fetchCurrentUser: options.fetchCurrentUser
  })

  return {
    lifecycle: {
      configureClientSession: clientSession.configureClientSession,
      hydrateClientSession: clientSession.hydrateClientSession,
      restoreClientSession: clientSession.restoreClientSession
    },
    auth: {
      loginWithWechat: clientSession.loginWithWechat,
      ensureClientSession: clientSession.ensureClientSession,
      clearClientSession: clientSession.clearClientSession,
      getCachedCurrentUser: clientSession.getCachedCurrentUser,
      refreshClientAccessToken: clientSession.refreshClientAccessToken
    },
    router: {
      appendQuery: router.appendQuery,
      replacePage: router.replacePage,
      reLaunchPage: router.reLaunchPage,
      openPrimaryPage: router.openPrimaryPage,
      goBackOr: router.goBackOr,
      redirectToLogin: router.redirectToLogin,
      buildCurrentPageUrl: router.buildCurrentPageUrl
    },
    payment: {
      requestWechatMiniappPayment: requestMiniappPayment
    },
    platform: {
      requestWechatLoginCode: requestLoginCode
    }
  }
}
