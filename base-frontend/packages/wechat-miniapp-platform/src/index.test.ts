import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  adminHttp,
  clientHttp,
  configureAdminHttp,
  configureClientHttp,
  configurePlatformHttp,
  platformHttp,
  type HttpTransport
} from '@forest/http-client'
import {
  configureMiniappHttpSession,
  createMiniappRouter,
  createSessionStore,
  createWxHttpTransport,
  requestWechatLoginCode,
  requestWechatMiniappPayment,
  type MiniappRuntime
} from './index'

function createRuntime(options: { pages?: Array<{ route: string, options?: Record<string, string> }> } = {}) {
  const storage = new Map<string, unknown>()
  const calls: Array<{ name: string, payload?: unknown }> = []
  const runtime: MiniappRuntime = {
    wx: {
      getStorageSync: vi.fn((key: string) => storage.get(key) || ''),
      setStorageSync: vi.fn((key: string, value: unknown) => {
        storage.set(key, value)
      }),
      removeStorageSync: vi.fn((key: string) => {
        storage.delete(key)
      }),
      request: vi.fn(),
      requestPayment: vi.fn(),
      login: vi.fn(),
      reLaunch: vi.fn((payload: unknown) => {
        calls.push({ name: 'reLaunch', payload })
      }),
      redirectTo: vi.fn((payload: unknown) => {
        calls.push({ name: 'redirectTo', payload })
      }),
      navigateBack: vi.fn((payload: unknown) => {
        calls.push({ name: 'navigateBack', payload })
      })
    },
    getApp: () => ({ globalData: {} }),
    getCurrentPages: () => options.pages || []
  }

  return { runtime, storage, calls }
}

describe('@forest/wechat-miniapp-platform', () => {
  afterEach(() => {
    configureClientHttp({})
    configureAdminHttp({})
    configurePlatformHttp({})
    vi.restoreAllMocks()
  })

  it('adapts wx.request into the shared http transport shape', async () => {
    const { runtime } = createRuntime()
    runtime.wx.request.mockImplementation((request: any) => {
      request.success({
        statusCode: 200,
        data: { ok: true },
        header: { Trace: 123 }
      })
    })

    const transport = createWxHttpTransport({ runtime })
    await expect(transport.request({
      url: 'https://forest.example/api',
      method: 'GET',
      params: { page: 1, empty: '' },
      headers: { Accept: 'application/json' }
    })).resolves.toMatchObject({
      status: 200,
      data: { ok: true },
      headers: { Trace: '123' }
    })

    expect(runtime.wx.request).toHaveBeenCalledWith(expect.objectContaining({
      url: 'https://forest.example/api?page=1',
      method: 'GET',
      header: { Accept: 'application/json' },
      timeout: 10000
    }))
  })

  it('namespaces session storage and avoids clearing a newer persisted token', () => {
    const { runtime, storage } = createRuntime()
    const store = createSessionStore({ storagePrefix: 'forest.test', runtime })

    store.saveSessionTokens('old-token', 'refresh-token')
    store.saveCachedUser({ id: 1 })
    storage.set('forest.test.accessToken', 'new-token')

    expect(store.clearSessionStore()).toBe(false)
    expect(storage.get('forest.test.accessToken')).toBe('new-token')
    expect(store.getAccessToken()).toBe('new-token')

    expect(store.clearSessionStore({ force: true })).toBe(true)
    expect(storage.has('forest.test.accessToken')).toBe(false)
    expect(storage.has('forest.test.refreshToken')).toBe(false)
    expect(storage.has('forest.test.currentUser')).toBe(false)
  })

  it('creates app-specific router helpers without hard-coding pages', () => {
    const { runtime, calls } = createRuntime({
      pages: [{ route: 'pages/leads/index', options: { keyword: 'phone' } }]
    })
    const router = createMiniappRouter({
      runtime,
      loginPage: '/pages/login/index',
      defaultPage: '/pages/leads/index',
      primaryPages: ['/pages/leads/index', '/pages/me/index']
    })

    expect(router.buildCurrentPageUrl()).toBe('/pages/leads/index?keyword=phone')
    expect(router.appendQuery('/pages/detail/index', { id: 3, empty: '' })).toBe('/pages/detail/index?id=3')

    router.redirectToLogin({ redirect: '/pages/leads/index?keyword=phone' })
    expect(calls[calls.length - 1]).toEqual({
      name: 'reLaunch',
      payload: expect.objectContaining({
        url: '/pages/login/index?redirect=%2Fpages%2Fleads%2Findex%3Fkeyword%3Dphone'
      })
    })

    router.openPrimaryPage('/pages/me/index?tab=profile')
    expect(calls[calls.length - 1]).toEqual({
      name: 'reLaunch',
      payload: expect.objectContaining({
        url: '/pages/me/index'
      })
    })
  })

  it('reports router failures and keeps replacePage routing choices', () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined)
    const { runtime, calls } = createRuntime({
      pages: [
        { route: 'pages/leads/index', options: {} },
        { route: 'pages/payment-result/index', options: {} }
      ]
    })
    const router = createMiniappRouter({
      runtime,
      loginPage: '/pages/login/index',
      defaultPage: '/pages/leads/index',
      primaryPages: ['/pages/leads/index', '/pages/me/index']
    })

    router.replacePage('/pages/lead-detail/index?id=3')
    expect(calls[calls.length - 1]).toEqual({
      name: 'redirectTo',
      payload: expect.objectContaining({
        url: '/pages/lead-detail/index?id=3'
      })
    })

    router.reLaunchPage('/pages/me/index')
    expect(calls[calls.length - 1]).toEqual({
      name: 'reLaunch',
      payload: expect.objectContaining({
        url: '/pages/me/index'
      })
    })

    const routeError = { errMsg: 'reLaunch:fail invalid url' }
    const fail = vi.fn()
    runtime.wx.reLaunch.mockImplementationOnce((request: any) => {
      request.fail(routeError)
    })

    router.reLaunchPage('/pages/missing/index', { fail })

    expect(fail).toHaveBeenCalledWith(routeError)
    expect(consoleErrorSpy).toHaveBeenCalledWith('[miniapp-router] route failed', {
      action: 'reLaunch',
      url: '/pages/missing/index',
      error: routeError
    })
  })

  it('wraps payment and login platform APIs', async () => {
    const { runtime } = createRuntime()
    runtime.wx.requestPayment.mockImplementation((request: any) => request.success())
    runtime.wx.login.mockImplementation((request: any) => request.success({ code: 'wx-code' }))

    await expect(requestWechatMiniappPayment({
      timeStamp: '1',
      nonceStr: 'nonce',
      packageValue: 'prepay_id=1',
      signType: 'RSA',
      paySign: 'sign'
    }, { runtime })).resolves.toBeUndefined()
    await expect(requestWechatLoginCode({ runtime })).resolves.toBe('wx-code')

    expect(runtime.wx.requestPayment).toHaveBeenCalledWith(expect.objectContaining({
      package: 'prepay_id=1'
    }))
  })

  it('configures shared client http with wx platform session data', async () => {
    const store = createSessionStore({ storagePrefix: 'forest.http', runtime: createRuntime().runtime })
    store.saveSessionTokens('access-token', 'refresh-token')
    const request = vi.fn(async () => ({
      status: 200,
      data: { code: 200, message: 'success', data: { ok: true } },
      headers: {}
    }))
    const transport: HttpTransport = {
      request: request as unknown as HttpTransport['request']
    }

    configureMiniappHttpSession({
      accessScope: 'CLIENT',
      apiBaseUrl: 'https://api.forest.example',
      sessionStore: store,
      transport,
      refreshAccessToken: async () => null,
      onUnauthorized: vi.fn()
    })

    await expect(clientHttp.get('/api/client/example')).resolves.toEqual({ ok: true })
    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: 'https://api.forest.example/api/client/example',
      headers: expect.objectContaining({
        Authorization: 'Bearer access-token'
      })
    }))
  })

  it('configures miniapp http session by access scope', async () => {
    const store = createSessionStore({ storagePrefix: 'forest.http', runtime: createRuntime().runtime })
    store.saveSessionTokens('access-token', 'refresh-token')
    const request = vi.fn(async () => ({
      status: 200,
      data: { code: 200, message: 'success', data: { ok: true } },
      headers: {}
    }))
    const transport: HttpTransport = {
      request: request as unknown as HttpTransport['request']
    }

    configureMiniappHttpSession({
      accessScope: 'ADMIN',
      apiBaseUrl: 'https://api.forest.example',
      sessionStore: store,
      transport,
      refreshAccessToken: async () => null,
      onUnauthorized: vi.fn()
    })

    await expect(adminHttp.get('/api/admin/example')).resolves.toEqual({ ok: true })
    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: 'https://api.forest.example/api/admin/example',
      headers: expect.objectContaining({
        Authorization: 'Bearer access-token'
      })
    }))

    configureMiniappHttpSession({
      accessScope: 'PLATFORM',
      apiBaseUrl: 'https://api.forest.example',
      sessionStore: store,
      transport,
      refreshAccessToken: async () => null,
      onUnauthorized: vi.fn()
    })

    await expect(platformHttp.get('/api/platform/example')).resolves.toEqual({ ok: true })
  })
})
