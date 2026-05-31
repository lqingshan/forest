import axios, { type AxiosAdapter, type AxiosRequestConfig, type AxiosResponse } from 'axios'

// 后端统一返回 Result<T>，这里定义的是那层通用外壳。
type ResultEnvelope<T> = {
  code: number
  message: string
  data: T
}

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export type HttpHeaders = Record<string, string>

export type HttpQueryParams = object

// transport 是真正发请求的运行时适配层；web/admin 默认用 axios，小程序运行时会注入 wx.request。
export interface HttpTransport {
  request<T = unknown>(request: HttpTransportRequest): Promise<HttpTransportResponse<T>>
}

export interface HttpTransportRequest {
  url: string
  method: HttpMethod
  headers: HttpHeaders
  params?: HttpQueryParams
  data?: unknown
  timeout?: number
  rawConfig?: HttpRequestConfig
}

export interface HttpTransportResponse<T = unknown> {
  status: number
  data: T
  headers?: HttpHeaders
  rawResponse?: unknown
}

// 认证能力由业务侧注入，http-client 只负责在请求链路里调用它们。
export interface HttpAuthProvider {
  getAccessToken?: () => string | null
  refreshAccessToken?: () => Promise<string | null>
  onUnauthorized?: () => void | Promise<void>
}

export interface HttpClientOptions extends HttpAuthProvider {
  baseUrl?: string
  transport?: HttpTransport
}

export const apiPaths = {
  api: '/api',
  auth: '/api/auth',
  client: '/api/client',
  admin: '/api/admin',
  platform: '/api/platform',
  open: '/api/open'
} as const

export interface HttpRequestConfig {
  params?: HttpQueryParams
  headers?: HttpHeaders | Record<string, unknown>
  timeout?: number
  withAuth?: boolean
  retryOn401?: boolean
  // 兼容既有测试和少量 web 场景：默认 axios transport 会继续把 adapter 透传给 axios。
  adapter?: AxiosAdapter
  signal?: AbortSignal
}

type InternalHttpRequestConfig = HttpRequestConfig & {
  __retried401?: boolean
}

// 暴露给业务包的最小接口：业务只关心 get/post/put/delete，不需要接触具体运行时。
export interface HttpClient {
  get<T>(url: string, config?: HttpRequestConfig): Promise<T>
  delete<T>(url: string, config?: HttpRequestConfig): Promise<T>
  post<T>(url: string, data?: unknown, config?: HttpRequestConfig): Promise<T>
  put<T>(url: string, data?: unknown, config?: HttpRequestConfig): Promise<T>
  patch<T>(url: string, data?: unknown, config?: HttpRequestConfig): Promise<T>
}

type HttpState = {
  authProvider: HttpAuthProvider
  baseUrl: string
  transport: HttpTransport
  // 多个请求同时 401 时，共用一次 refresh，避免并发刷新 token。
  refreshPromise: Promise<string | null> | null
}

// 统一错误对象，方便业务层拿到 HTTP status / 业务 code / 原始 response。
export class HttpError extends Error {
  status: number | null
  code: number | null
  response?: HttpTransportResponse<unknown>

  constructor(message: string, options: { status?: number | null, code?: number | null, response?: HttpTransportResponse<unknown> } = {}) {
    super(message)
    this.name = 'HttpError'
    this.status = options.status ?? null
    this.code = options.code ?? null
    this.response = options.response
  }
}

// 生成一组“可配置认证策略和请求 transport 的 http client”。
// admin/client 各自持有一组独立 state，所以它们的 token、401 处理可以分开。
function createHttpClientPair() {
  const state: HttpState = {
    authProvider: {},
    baseUrl: '',
    transport: createAxiosTransport(),
    refreshPromise: null
  }

  return {
    client: wrapState(state),
    configure(options: HttpClientOptions = {}) {
      state.authProvider = pickAuthProvider(options)
      state.baseUrl = options.baseUrl ?? ''
      state.transport = options.transport ?? createAxiosTransport()
      // 每次重新配置 provider 时，把旧的 refresh 状态清掉，避免把旧会话带进来。
      state.refreshPromise = null
    }
  }
}

function pickAuthProvider(options: HttpClientOptions): HttpAuthProvider {
  return {
    getAccessToken: options.getAccessToken,
    refreshAccessToken: options.refreshAccessToken,
    onUnauthorized: options.onUnauthorized
  }
}

// 把请求能力收口成我们自己的 HttpClient 接口。
// 业务层以后即使从 axios 换到 wx.request，也不需要整体改 API 调用方式。
function wrapState(state: HttpState): HttpClient {
  return {
    get<T>(url: string, config?: HttpRequestConfig) {
      return request<T>(state, 'GET', url, undefined, config)
    },
    delete<T>(url: string, config?: HttpRequestConfig) {
      return request<T>(state, 'DELETE', url, undefined, config)
    },
    post<T>(url: string, data?: unknown, config?: HttpRequestConfig) {
      return request<T>(state, 'POST', url, data, config)
    },
    put<T>(url: string, data?: unknown, config?: HttpRequestConfig) {
      return request<T>(state, 'PUT', url, data, config)
    },
    patch<T>(url: string, data?: unknown, config?: HttpRequestConfig) {
      return request<T>(state, 'PATCH', url, data, config)
    }
  }
}

async function request<T>(
  state: HttpState,
  method: HttpMethod,
  url: string,
  data?: unknown,
  config: InternalHttpRequestConfig = {}
): Promise<T> {
  const transportRequest = buildTransportRequest(state, method, url, data, config)

  try {
    const response = await state.transport.request<ResultEnvelope<T> | T>(transportRequest)
    if (response.status === 401) {
      return handleUnauthorizedResponse(state, method, url, data, config, response)
    }

    return unwrapResponse<T>(response)
  } catch (error) {
    const httpError = toHttpError(error)
    if (httpError.status === 401) {
      return handleUnauthorizedError(state, method, url, data, config, httpError)
    }
    throw httpError
  }
}

function buildTransportRequest(
  state: HttpState,
  method: HttpMethod,
  url: string,
  data: unknown,
  config: InternalHttpRequestConfig
): HttpTransportRequest {
  const headers = normalizeHeaders(config.headers)
  setHeaderIfMissing(headers, 'Accept', 'application/json')

  if (data !== undefined && !isFormDataPayload(data)) {
    setHeaderIfMissing(headers, 'Content-Type', 'application/json')
  }

  if (config.withAuth !== false) {
    const token = state.authProvider.getAccessToken?.()
    if (token && (!hasHeader(headers, 'Authorization') || config.__retried401)) {
      setHeader(headers, 'Authorization', `Bearer ${token}`)
    }
  }

  return {
    url: resolveUrl(state.baseUrl, url),
    method,
    headers,
    params: config.params,
    data,
    timeout: config.timeout,
    rawConfig: config
  }
}

async function handleUnauthorizedResponse<T>(
  state: HttpState,
  method: HttpMethod,
  url: string,
  data: unknown,
  config: InternalHttpRequestConfig,
  response: HttpTransportResponse<unknown>
): Promise<T> {
  if (shouldRetryUnauthorized(config, state.authProvider)) {
    const refreshed = await refreshAccessToken(state)
    if (refreshed) {
      return request<T>(state, method, url, data, {
        ...config,
        __retried401: true
      })
    }
    await state.authProvider.onUnauthorized?.()
  } else if (config.withAuth !== false) {
    await state.authProvider.onUnauthorized?.()
  }

  throw responseToHttpError(response)
}

async function handleUnauthorizedError<T>(
  state: HttpState,
  method: HttpMethod,
  url: string,
  data: unknown,
  config: InternalHttpRequestConfig,
  error: HttpError
): Promise<T> {
  if (shouldRetryUnauthorized(config, state.authProvider)) {
    const refreshed = await refreshAccessToken(state)
    if (refreshed) {
      return request<T>(state, method, url, data, {
        ...config,
        __retried401: true
      })
    }
    await state.authProvider.onUnauthorized?.()
  } else if (config.withAuth !== false) {
    await state.authProvider.onUnauthorized?.()
  }

  throw error
}

// 后端接口大部分都走 Result<T>，这里把它直接解成业务真正需要的 data。
// 如果后面某个接口返回的不是 Result<T>，这里也允许原样透传。
function unwrapResponse<T>(response: HttpTransportResponse<ResultEnvelope<T> | T>): T {
  const body = response.data

  if (response.status < 200 || response.status >= 300) {
    throw responseToHttpError(response)
  }

  if (isResultEnvelope<T>(body)) {
    if (body.code !== 200) {
      throw new HttpError(body.message || '请求失败', {
        status: response.status,
        code: body.code,
        response: response as HttpTransportResponse<unknown>
      })
    }
    return body.data
  }

  return body as T
}

function isResultEnvelope<T>(value: unknown): value is ResultEnvelope<T> {
  return value != null
    && typeof value === 'object'
    && 'code' in value
    && 'message' in value
}

// 判断当前 401 是否值得“自动 refresh 后再试一次”。
// 这层只判断条件，不执行副作用，方便读代码时把决策和动作分开。
function shouldRetryUnauthorized(
  config: InternalHttpRequestConfig,
  provider: HttpAuthProvider
) {
  return Boolean(
    config.withAuth !== false
    && config.retryOn401 !== false
    && !config.__retried401
    && provider.refreshAccessToken
  )
}

// 同一时刻只允许一个 refresh 在飞；后续请求直接等待它的结果。
async function refreshAccessToken(state: HttpState) {
  if (!state.authProvider.refreshAccessToken) {
    return null
  }

  if (!state.refreshPromise) {
    state.refreshPromise = Promise.resolve(state.authProvider.refreshAccessToken())
      .finally(() => {
        state.refreshPromise = null
      })
  }

  return state.refreshPromise
}

function createAxiosTransport(): HttpTransport {
  const instance = axios.create({
    // 非 2xx 也交给统一 HTTP 管线处理，避免 axios 和 wx.request 行为分叉。
    validateStatus: () => true
  })

  return {
    async request<T>(request: HttpTransportRequest): Promise<HttpTransportResponse<T>> {
      const config: AxiosRequestConfig = {
        url: request.url,
        method: request.method,
        params: request.params,
        data: request.data,
        headers: request.headers,
        timeout: request.timeout,
        adapter: request.rawConfig?.adapter,
        signal: request.rawConfig?.signal
      }
      const response = await instance.request<T>(config)
      return fromAxiosResponse(response)
    }
  }
}

function fromAxiosResponse<T>(response: AxiosResponse<T>): HttpTransportResponse<T> {
  return {
    status: response.status,
    data: response.data,
    headers: normalizeHeaders(response.headers as Record<string, unknown>),
    rawResponse: response
  }
}

function responseToHttpError(response: HttpTransportResponse<unknown>) {
  const result = isResultEnvelope<unknown>(response.data) ? response.data : null
  return new HttpError(result?.message || '请求失败', {
    status: response.status,
    code: typeof result?.code === 'number' ? result.code : null,
    response
  })
}

// 把各种来源的异常都归一到 HttpError，业务层就不用分别判断 axios / 原生 Error / wx fail。
function toHttpError(error: unknown) {
  if (error instanceof HttpError) {
    return error
  }

  if (axios.isAxiosError(error)) {
    const response = error.response ? fromAxiosResponse(error.response) : undefined
    const result = response && isResultEnvelope<unknown>(response.data) ? response.data : null
    return new HttpError(result?.message || error.message || '请求失败', {
      status: response?.status ?? null,
      code: typeof result?.code === 'number' ? result.code : null,
      response
    })
  }

  if (error instanceof Error) {
    return new HttpError(error.message)
  }

  return new HttpError('请求失败')
}

function resolveUrl(baseUrl: string, url: string) {
  if (!baseUrl || /^https?:\/\//i.test(url)) {
    return url
  }

  return `${baseUrl.replace(/\/$/, '')}/${url.replace(/^\//, '')}`
}

function normalizeHeaders(input?: HttpRequestConfig['headers']): HttpHeaders {
  const headers: HttpHeaders = {}
  if (!input) {
    return headers
  }

  if (typeof (input as { forEach?: unknown }).forEach === 'function') {
    ;(input as { forEach: (callback: (value: unknown, key: string) => void) => void }).forEach((value, key) => {
      if (value !== undefined && value !== null) {
        headers[key] = String(value)
      }
    })
    return headers
  }

  for (const [key, value] of Object.entries(input)) {
    if (value !== undefined && value !== null) {
      headers[key] = String(value)
    }
  }
  return headers
}

function hasHeader(headers: HttpHeaders, name: string) {
  return Object.keys(headers).some((key) => key.toLowerCase() === name.toLowerCase())
}

function setHeaderIfMissing(headers: HttpHeaders, name: string, value: string) {
  if (!hasHeader(headers, name)) {
    headers[name] = value
  }
}

function setHeader(headers: HttpHeaders, name: string, value: string) {
  const existingKey = Object.keys(headers).find((key) => key.toLowerCase() === name.toLowerCase())
  headers[existingKey || name] = value
}

function isFormDataPayload(data: unknown) {
  return typeof FormData !== 'undefined' && data instanceof FormData
}

// platform/admin/client 三套 client 共享同一套技术实现，但认证策略和 transport 完全独立。
const platformPair = createHttpClientPair()
const adminPair = createHttpClientPair()
const clientPair = createHttpClientPair()

export const platformHttp = platformPair.client
export const adminHttp = adminPair.client
export const clientHttp = clientPair.client

// app 启动时调用这里，把当前端的 token/refresh/logout/transport 规则注入进来。
export function configurePlatformHttp(options: HttpClientOptions = {}) {
  platformPair.configure(options)
}

export function configureAdminHttp(options: HttpClientOptions = {}) {
  adminPair.configure(options)
}

export function configureClientHttp(options: HttpClientOptions = {}) {
  clientPair.configure(options)
}

export type AccessScope = 'CLIENT' | 'ADMIN' | 'PLATFORM'

// 根据 accessScope 选择对应的 HTTP client。accessScope 只表达 API 访问面，
// 不表达当前运行时；同一个 client 可以被配置成 axios、wx.request 或测试 transport。
export function httpForAccessScope(accessScope: string): HttpClient {
  switch (normalizeAccessScope(accessScope)) {
    case 'CLIENT':
      return clientHttp
    case 'ADMIN':
      return adminHttp
    case 'PLATFORM':
      return platformHttp
  }
}

// 根据 accessScope 配置对应 HTTP client 的 token、refresh、transport 等规则。
export function configureHttpForAccessScope(accessScope: string, options: HttpClientOptions = {}) {
  switch (normalizeAccessScope(accessScope)) {
    case 'CLIENT':
      configureClientHttp(options)
      break
    case 'ADMIN':
      configureAdminHttp(options)
      break
    case 'PLATFORM':
      configurePlatformHttp(options)
      break
  }
}

// 根据 accessScope 推导业务 API 顶层前缀。
export function apiPathForAccessScope(accessScope: string) {
  switch (normalizeAccessScope(accessScope)) {
    case 'CLIENT':
      return apiPaths.client
    case 'ADMIN':
      return apiPaths.admin
    case 'PLATFORM':
      return apiPaths.platform
  }
}

function normalizeAccessScope(accessScope: string): AccessScope {
  const normalized = accessScope?.trim().toUpperCase()
  if (normalized === 'CLIENT' || normalized === 'ADMIN' || normalized === 'PLATFORM') {
    return normalized
  }
  throw new Error(`未知 accessScope: ${accessScope || '空'}`)
}
