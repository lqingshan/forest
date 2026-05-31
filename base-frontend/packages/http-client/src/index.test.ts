import { afterEach, describe, expect, it, vi } from 'vitest'
import { AxiosHeaders } from 'axios'
import {
  adminHttp,
  apiPaths,
  apiPathForAccessScope,
  clientHttp,
  configureAdminHttp,
  configureClientHttp,
  configureHttpForAccessScope,
  configurePlatformHttp,
  HttpError,
  httpForAccessScope,
  platformHttp,
  type HttpTransportRequest,
  type HttpTransportResponse,
  type HttpTransport
} from './index'

function createMockTransport(handler: (request: HttpTransportRequest) => Promise<HttpTransportResponse<unknown>>) {
  const request = vi.fn(handler)
  const transport: HttpTransport = {
    request: request as unknown as HttpTransport['request']
  }
  return { transport, request }
}

describe('@forest/http-client', () => {
  afterEach(() => {
    configurePlatformHttp({})
    configureAdminHttp({})
    configureClientHttp({})
    vi.restoreAllMocks()
  })

  it('exposes shared api path prefixes', () => {
    expect(apiPaths).toMatchObject({
      auth: '/api/auth',
      client: '/api/client',
      admin: '/api/admin',
      platform: '/api/platform',
      open: '/api/open'
    })
  })

  it('maps access scopes to api prefixes and scoped http clients', () => {
    expect(httpForAccessScope('CLIENT')).toBe(clientHttp)
    expect(httpForAccessScope('ADMIN')).toBe(adminHttp)
    expect(httpForAccessScope('PLATFORM')).toBe(platformHttp)

    expect(apiPathForAccessScope('CLIENT')).toBe('/api/client')
    expect(apiPathForAccessScope('ADMIN')).toBe('/api/admin')
    expect(apiPathForAccessScope('PLATFORM')).toBe('/api/platform')
  })

  it('configures the scoped http client selected by access scope', async () => {
    const { transport, request } = createMockTransport(async () => ({
      data: { code: 200, message: 'success', data: { ok: true } },
      status: 200,
      headers: {}
    }))

    configureHttpForAccessScope('ADMIN', {
      baseUrl: 'https://api.forest.example',
      transport,
      getAccessToken: () => 'admin-token'
    })

    await expect(adminHttp.get<{ ok: boolean }>('/api/admin/example')).resolves.toEqual({ ok: true })
    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: 'https://api.forest.example/api/admin/example',
      headers: expect.objectContaining({
        Authorization: 'Bearer admin-token'
      })
    }))
  })

  it('unwraps result envelopes and attaches admin bearer token through default axios transport', async () => {
    configureAdminHttp({
      getAccessToken: () => 'admin-token'
    })

    const adapter = vi.fn(async (config) => ({
      data: {
        code: 200,
        message: 'success',
        data: { ok: true }
      },
      status: 200,
      statusText: 'OK',
      headers: {},
      config
    }))

    await expect(adminHttp.get<{ ok: boolean }>('/api/admin/example', { adapter })).resolves.toEqual({ ok: true })

    const requestConfig = adapter.mock.calls[0][0]
    const headers = AxiosHeaders.from(requestConfig.headers)
    expect(headers.get('Accept')).toBe('application/json')
    expect(headers.get('Authorization')).toBe('Bearer admin-token')
  })

  it('keeps platform http state separate from admin and client http state', async () => {
    const { transport, request } = createMockTransport(async () => ({
      data: { code: 200, message: 'success', data: { ok: true } },
      status: 200,
      headers: {}
    }))

    configurePlatformHttp({
      baseUrl: 'https://api.forest.example',
      transport,
      getAccessToken: () => 'platform-token'
    })

    await expect(platformHttp.get<{ ok: boolean }>(`${apiPaths.platform}/example`)).resolves.toEqual({ ok: true })

    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: 'https://api.forest.example/api/platform/example',
      headers: expect.objectContaining({
        Authorization: 'Bearer platform-token'
      })
    }))
  })

  it('allows app shells to inject a platform transport and baseUrl', async () => {
    const { transport, request } = createMockTransport(async () => ({
      data: { code: 200, message: 'success', data: { ok: true } },
      status: 200,
      headers: {}
    }))

    configureClientHttp({
      baseUrl: 'https://api.forest.example',
      transport,
      getAccessToken: () => 'client-token'
    })

    await expect(clientHttp.get<{ ok: boolean }>('/api/client/example', {
      params: { page: 1 }
    })).resolves.toEqual({ ok: true })

    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: 'https://api.forest.example/api/client/example',
      method: 'GET',
      params: { page: 1 },
      headers: expect.objectContaining({
        Accept: 'application/json',
        Authorization: 'Bearer client-token'
      })
    }))
  })

  it('retries client request once after refresh succeeds', async () => {
    let accessToken = 'expired-token'
    const refreshAccessToken = vi.fn(async () => {
      accessToken = 'fresh-token'
      return accessToken
    })
    const { transport, request } = createMockTransport(async (request) => {
      if (request.headers.Authorization === 'Bearer expired-token') {
        return {
          data: { code: 401, message: '未登录', data: null },
          status: 401,
          headers: {}
        }
      }

      return {
        data: { code: 200, message: 'success', data: { ok: true } },
        status: 200,
        headers: {}
      }
    })

    configureClientHttp({
      transport,
      getAccessToken: () => accessToken,
      refreshAccessToken
    })

    await expect(clientHttp.get<{ ok: boolean }>('/api/client/example')).resolves.toEqual({ ok: true })
    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(request).toHaveBeenCalledTimes(2)
    expect(request).toHaveBeenLastCalledWith(expect.objectContaining({
      headers: expect.objectContaining({
        Authorization: 'Bearer fresh-token'
      })
    }))
  })

  it('calls onUnauthorized when client refresh fails', async () => {
    const onUnauthorized = vi.fn()
    const { transport, request } = createMockTransport(async () => ({
      data: { code: 401, message: '登录已失效', data: null },
      status: 401,
      headers: {}
    }))

    configureClientHttp({
      transport,
      getAccessToken: () => 'expired-token',
      refreshAccessToken: async () => null,
      onUnauthorized
    })

    await expect(clientHttp.get('/api/client/example')).rejects.toThrow('登录已失效')
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
    expect(request).toHaveBeenCalledTimes(1)
  })

  it('throws backend business message for non-200 result codes', async () => {
    const { transport } = createMockTransport(async () => ({
      data: { code: 500, message: '业务失败', data: null },
      status: 200,
      headers: {}
    }))

    configureAdminHttp({ transport })

    await expect(adminHttp.get('/api/admin/example')).rejects.toMatchObject({
      message: '业务失败',
      code: 500,
      status: 200
    } satisfies Partial<HttpError>)
  })

  it('supports unauthenticated client requests', async () => {
    const { transport, request } = createMockTransport(async () => ({
      data: { code: 200, message: 'success', data: { accessToken: 'token' } },
      status: 200,
      headers: {}
    }))

    configureClientHttp({
      transport,
      getAccessToken: () => 'should-not-be-used'
    })

    await clientHttp.post('/api/client/account/wechat/login', { code: 'wx-code' }, {
      withAuth: false,
      retryOn401: false
    })

    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      headers: expect.not.objectContaining({
        Authorization: expect.any(String)
      })
    }))
  })
})
