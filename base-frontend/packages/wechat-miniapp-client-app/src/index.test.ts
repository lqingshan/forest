import { afterEach, describe, expect, it, vi } from 'vitest'
import { configureAdminHttp, configureClientHttp, configurePlatformHttp } from '@forest/http-client'
import { createWechatMiniappClientApp } from './index'
import type { MiniappRuntime } from '@forest/wechat-miniapp-platform'

function createRuntime() {
  const storage = new Map<string, unknown>()
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
      reLaunch: vi.fn(),
      redirectTo: vi.fn(),
      navigateBack: vi.fn()
    },
    getApp: () => ({ globalData: {} }),
    getCurrentPages: () => []
  }

  return { runtime, storage }
}

describe('@forest/wechat-miniapp-client-app', () => {
  afterEach(() => {
    configureClientHttp({})
    configureAdminHttp({})
    configurePlatformHttp({})
    vi.restoreAllMocks()
  })

  it('assembles session, router and payment into a single app facade', async () => {
    const { runtime, storage } = createRuntime()
    runtime.wx.login.mockImplementation((request: any) => {
      request.success({ code: 'wx-code' })
    })
    runtime.wx.requestPayment.mockImplementation((request: any) => {
      request.success()
    })

    const app = createWechatMiniappClientApp({
      storagePrefix: 'forest.test',
      apiBaseUrl: 'https://api.forest.example',
      accessScope: 'CLIENT',
      loginPage: '/pages/login/index',
      defaultPage: '/pages/leads/index',
      primaryPages: ['/pages/leads/index', '/pages/me/index'],
      runtime,
      loginByWechat: vi.fn(async (code: string) => ({
        accessToken: `access-${code}`,
        refreshToken: 'refresh-token'
      })),
      refreshAccessToken: vi.fn(async () => ({
        accessToken: 'refreshed-token'
      })),
      fetchCurrentUser: vi.fn(async () => ({
        id: 7,
        name: 'forest-user'
      }))
    })

    await expect(app.auth.loginWithWechat()).resolves.toMatchObject({
      currentUser: {
        id: 7,
        name: 'forest-user'
      }
    })
    expect(storage.get('forest.test.accessToken')).toBe('access-wx-code')
    expect(app.auth.getCachedCurrentUser()).toEqual({
      id: 7,
      name: 'forest-user'
    })

    await expect(app.payment.requestWechatMiniappPayment({
      timeStamp: '1',
      nonceStr: 'nonce',
      packageValue: 'prepay_id=1',
      signType: 'RSA',
      paySign: 'sign'
    })).resolves.toBeUndefined()
    expect(runtime.wx.requestPayment).toHaveBeenCalledWith(expect.objectContaining({
      package: 'prepay_id=1'
    }))
  })
})
