import {
  configureHttpForAccessScope,
  type HttpQueryParams,
  type HttpTransport,
  type HttpTransportRequest,
  type HttpTransportResponse
} from '@forest/http-client'
import { getRuntime, type MiniappRecord, type MiniappRuntime, type MiniappRuntimeOptions } from './runtime'
import type { MiniappSessionStore } from './session-store'

/**
 * 微信小程序 HTTP transport 适配。
 *
 * 这个模块把 wx.request 转成 @forest/http-client 能识别的统一 transport，
 * 并负责把通用 HTTP client 配置成“小程序登录态 + wx.request”模式。
 */

/**
 * 把通用 HTTP client 配置成“小程序运行时模式”需要的参数。
 *
 * - apiBaseUrl：后端地址
 * - accessScope：决定配置 clientHttp / adminHttp / platformHttp 哪一套 auth state
 * - sessionStore：提供 accessToken/currentUser 等登录态
 * - refreshAccessToken：401 时如何刷新 token，由 app auth/session 层注入
 * - onUnauthorized：刷新失败或无权限时如何处理，由 app auth/session 层注入
 * - transport：默认使用 wx.request，也允许测试注入假 transport
 */
export interface ConfigureMiniappHttpSessionOptions extends MiniappRuntimeOptions {
  accessScope: string
  apiBaseUrl: string
  sessionStore: MiniappSessionStore
  refreshAccessToken: () => Promise<string | null>
  onUnauthorized: () => Promise<void> | void
  transport?: HttpTransport
}

/**
 * 创建 @forest/http-client 可识别的 transport。
 *
 * @forest/http-client 不关心底层是 axios、fetch、wx.request 还是测试假实现。
 * 它只要求 transport.request 返回统一的 HttpTransportResponse。
 *
 * 这里就是把 wx.request 的 callback API 包装成 Promise，并转换成统一响应结构。
 */
export function createWxHttpTransport(options: MiniappRuntimeOptions = {}): HttpTransport {
  return {
    request<T>(request: HttpTransportRequest) {
      return requestWithWx<T>(request, options.runtime)
    }
  }
}

/**
 * 把共享 HTTP client 配置为“小程序登录态 + wx.request transport”。
 *
 * @forest/http-client 是跨 Web/小程序复用的 HTTP 门面。
 * 小程序环境不能直接用 axios/fetch adapter，因此这里把 transport 换成 wx.request。
 *
 * 注意：小程序多入口 bundle 下，app.js 和 page.js 可能各有一份模块状态。
 * 因此上层 session facade 会在请求入口幂等调用这个配置，确保当前 bundle 内
 * 的 scoped HTTP client 也已经切换到 wx.request。
 */
export function configureMiniappHttpSession(options: ConfigureMiniappHttpSessionOptions) {
  configureHttpForAccessScope(options.accessScope, {
    baseUrl: options.apiBaseUrl,
    transport: options.transport || createWxHttpTransport({ runtime: options.runtime }),
    getAccessToken: () => options.sessionStore.getAccessToken() || null,
    refreshAccessToken: options.refreshAccessToken,
    onUnauthorized: options.onUnauthorized
  })
}

/**
 * wx.request -> HttpTransportResponse 的核心适配函数。
 *
 * 统一 HTTP client 需要的是：
 * - status
 * - data
 * - headers
 * - rawResponse
 *
 * 微信给的是：
 * - statusCode
 * - data
 * - header
 *
 * 这里负责字段映射、query 拼接、默认 timeout、错误消息转换。
 */
function requestWithWx<T>(request: HttpTransportRequest, runtime?: MiniappRuntime): Promise<HttpTransportResponse<T>> {
  return new Promise((resolve, reject) => {
    getRuntime(runtime).wx.request({
      url: appendParams(request.url, request.params),
      method: request.method,
      data: request.data,
      timeout: request.timeout || 10000,
      header: request.headers,
      success: (response: MiniappRecord) => {
        resolve({
          status: response.statusCode,
          data: response.data as T,
          headers: normalizeResponseHeaders(response.header),
          rawResponse: response
        })
      },
      fail: (error: MiniappRecord) => {
        const reason = error.errMsg ? `：${error.errMsg}` : ''
        reject(new Error(`网络请求失败${reason}`))
      }
    })
  })
}

/**
 * 把 params 拼到 url 后面。
 *
 * 这个函数和 router.appendQuery 很像，但这里服务 HTTP 请求；
 * router.appendQuery 服务小程序页面 URL。
 */
function appendParams(url: string, params?: HttpQueryParams) {
  const entries = Object.entries(params || {}).filter(([, value]) => value !== undefined && value !== null && value !== '')
  if (!entries.length) {
    return url
  }
  const query = entries
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&')
  return `${url}${url.includes('?') ? '&' : '?'}${query}`
}

/**
 * 微信响应 header 的值可能不是字符串。统一转成 Record<string, string>，
 * 让 @forest/http-client 后续逻辑不用关心平台差异。
 */
function normalizeResponseHeaders(headers: unknown) {
  const output: Record<string, string> = {}
  if (!headers || typeof headers !== 'object') {
    return output
  }
  for (const [key, value] of Object.entries(headers as Record<string, unknown>)) {
    if (value !== undefined && value !== null) {
      output[key] = String(value)
    }
  }
  return output
}
