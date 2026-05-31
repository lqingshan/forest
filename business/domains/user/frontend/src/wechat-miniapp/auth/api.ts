import { apiPaths, clientHttp, httpForAccessScope, type HttpClient } from '@forest/http-client'
import type {
  RefreshTokenResult,
  WechatLoginContext,
  WechatLoginResult,
  WechatMiniappAuthApi,
  WechatMiniappAuthApiOptions
} from './types'

const API_BASE = apiPaths.auth
const DEFAULT_WECHAT_MINIAPP_CLIENT_TYPE = 'WECHAT_MINIAPP'
const DEFAULT_WECHAT_MINIAPP_ACCESS_SCOPE = 'CLIENT'

export function createWechatMiniappAuthApi(options: WechatMiniappAuthApiOptions): WechatMiniappAuthApi {
  const appCode = normalizeRequiredCode(options.appCode, 'appCode')
  const clientType = normalizeRequiredCode(options.clientType || DEFAULT_WECHAT_MINIAPP_CLIENT_TYPE, 'clientType')
  const accessScope = normalizeRequiredCode(options.accessScope || DEFAULT_WECHAT_MINIAPP_ACCESS_SCOPE, 'accessScope')
  const httpClient = httpForAccessScope(accessScope)

  return {
    loginByWechat(code: string) {
      return postWechatMiniappLogin(code, {
        appCode,
        clientType,
        accessScope
      }, httpClient)
    },
    refreshAccessToken(refreshToken: string) {
      return refreshAccessToken(refreshToken, { accessScope })
    }
  }
}

export function createWechatMiniappPhoneAuthApi(options: WechatMiniappAuthApiOptions): WechatMiniappAuthApi {
  const appCode = normalizeRequiredCode(options.appCode, 'appCode')
  const clientType = normalizeRequiredCode(options.clientType || DEFAULT_WECHAT_MINIAPP_CLIENT_TYPE, 'clientType')
  const accessScope = normalizeRequiredCode(options.accessScope || DEFAULT_WECHAT_MINIAPP_ACCESS_SCOPE, 'accessScope')
  const httpClient = httpForAccessScope(accessScope)

  return {
    async loginByWechat(code: string, context: WechatLoginContext = {}) {
      return postWechatMiniappPhoneLogin(code, context, {
        appCode,
        clientType,
        accessScope
      }, httpClient)
    },
    refreshAccessToken(refreshToken: string) {
      return refreshAccessToken(refreshToken, { accessScope })
    }
  }
}

// 小程序端上传 wx.login code；openid 解析、账号绑定和 JWT 签发都在后端完成。
function postWechatMiniappLogin(
  code: string,
  options: Required<WechatMiniappAuthApiOptions>,
  httpClient: HttpClient = clientHttp
) {
  return httpClient.post<WechatLoginResult>(`${API_BASE}/wechat-miniapp/login`, {
    code,
    clientType: options.clientType,
    appCode: options.appCode,
    accessScope: options.accessScope
  }, {
    withAuth: false,
    retryOn401: false
  })
}

// 小程序端上传 wx.login code 和微信手机号授权 code；openid/手机号解析、账号绑定和 JWT 签发都在后端完成。
function postWechatMiniappPhoneLogin(
  code: string,
  context: WechatLoginContext,
  options: Required<WechatMiniappAuthApiOptions>,
  httpClient: HttpClient = clientHttp
) {
  const phoneCode = normalizeRequiredCode(context.phoneCode || '', 'phoneCode')
  return httpClient.post<WechatLoginResult>(`${API_BASE}/wechat-miniapp/phone-login`, {
    code,
    phoneCode,
    clientType: options.clientType,
    appCode: options.appCode,
    accessScope: options.accessScope
  }, {
    withAuth: false,
    retryOn401: false
  })
}

// 刷新接口不携带 access token，避免 401 场景下形成鉴权死循环。
export function refreshAccessToken(refreshToken: string, options: Pick<WechatMiniappAuthApiOptions, 'accessScope'> = {}) {
  const httpClient = httpForAccessScope(options.accessScope || DEFAULT_WECHAT_MINIAPP_ACCESS_SCOPE)
  return httpClient.post<RefreshTokenResult>(`${API_BASE}/refresh`, { refreshToken }, {
    withAuth: false,
    retryOn401: false
  })
}

function normalizeRequiredCode(value: string, fieldName: string) {
  const normalized = value.trim()
  if (!normalized) {
    throw new Error(`${fieldName} 不能为空`)
  }
  return normalized
}
