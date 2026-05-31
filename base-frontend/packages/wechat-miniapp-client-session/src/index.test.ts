import { afterEach, describe, expect, it, vi } from 'vitest'
import { createSessionStore, type MiniappRouter, type MiniappStorage } from '@forest/wechat-miniapp-platform'
import { createWechatClientSession } from './index'

interface CurrentUser {
  id: number
  name: string
}

function createMemoryStorage() {
  const storage = new Map<string, unknown>()
  const api: MiniappStorage = {
    getString: (key: string) => String(storage.get(key) || ''),
    setString: (key: string, value: string) => {
      if (!value) {
        storage.delete(key)
        return
      }
      storage.set(key, value)
    },
    getJson: <T>(key: string) => (storage.get(key) || null) as T | null,
    setJson: (key: string, value: unknown) => {
      if (value == null) {
        storage.delete(key)
        return
      }
      storage.set(key, value)
    }
  }
  return {
    storage,
    api
  }
}

function createHarness() {
  const { api } = createMemoryStorage()
  const sessionStore = createSessionStore({ storagePrefix: 'forest.session', storage: api })
  const router: Pick<MiniappRouter, 'buildCurrentPageUrl' | 'redirectToLogin'> = {
    buildCurrentPageUrl: vi.fn(() => '/pages/leads/index?keyword=phone'),
    redirectToLogin: vi.fn()
  }
  const configureHttpSession = vi.fn()
  const requestLoginCode = vi.fn(async () => 'wx-code')
  const loginByWechat = vi.fn(async () => ({
    accessToken: 'access-token',
    refreshToken: 'refresh-token'
  }))
  const refreshAccessToken = vi.fn(async () => ({
    accessToken: 'fresh-token'
  }))
  const fetchCurrentUser = vi.fn(async () => ({
    id: 1,
    name: 'Alice'
  }))
  const clientSession = createWechatClientSession<CurrentUser, { accessToken: string, refreshToken: string }>({
    sessionStore,
    router,
    configureHttpSession,
    requestLoginCode,
    loginByWechat,
    refreshAccessToken,
    fetchCurrentUser
  })

  return {
    sessionStore,
    router,
    configureHttpSession,
    requestLoginCode,
    loginByWechat,
    refreshAccessToken,
    fetchCurrentUser,
    clientSession
  }
}

describe('@forest/wechat-miniapp-client-session', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('configures http 401 handling through injected app router and store', async () => {
    const harness = createHarness()

    harness.clientSession.configureClientSession()
    harness.clientSession.hydrateClientSession()

    const httpOptions = harness.configureHttpSession.mock.calls[0][0]
    expect(harness.configureHttpSession).toHaveBeenCalledTimes(1)
    harness.sessionStore.saveSessionTokens('expired-token', 'refresh-token')
    await httpOptions.onUnauthorized()

    expect(harness.router.redirectToLogin).toHaveBeenCalledWith({
      redirect: '/pages/leads/index?keyword=phone'
    })
    expect(harness.sessionStore.getAccessToken()).toBe('')
  })

  it('logs in with injected wx code and user APIs, then caches the current user', async () => {
    const harness = createHarness()

    await expect(harness.clientSession.loginWithWechat()).resolves.toEqual({
      loginResult: {
        accessToken: 'access-token',
        refreshToken: 'refresh-token'
      },
      currentUser: {
        id: 1,
        name: 'Alice'
      }
    })

    expect(harness.requestLoginCode).toHaveBeenCalledTimes(1)
    expect(harness.loginByWechat).toHaveBeenCalledWith('wx-code', undefined)
    expect(harness.fetchCurrentUser).toHaveBeenCalledTimes(1)
    expect(harness.configureHttpSession).toHaveBeenCalledTimes(1)
    expect(harness.clientSession.getCachedCurrentUser()).toEqual({
      id: 1,
      name: 'Alice'
    })
  })

  it('restores existing access token without redirecting from login page code', async () => {
    const harness = createHarness()
    harness.sessionStore.saveSessionTokens('access-token', 'refresh-token')

    const firstRestore = harness.clientSession.restoreClientSession()
    const secondRestore = harness.clientSession.restoreClientSession()
    await expect(firstRestore).resolves.toBe(true)
    await expect(secondRestore).resolves.toBe(true)

    expect(harness.fetchCurrentUser).toHaveBeenCalledTimes(1)
    expect(harness.configureHttpSession).toHaveBeenCalledTimes(1)
    expect(harness.clientSession.getCachedCurrentUser()).toEqual({
      id: 1,
      name: 'Alice'
    })
    expect(harness.router.redirectToLogin).not.toHaveBeenCalled()
  })

  it('redirects to login when ensure session has no access token', async () => {
    const harness = createHarness()

    await expect(harness.clientSession.ensureClientSession({ redirect: '/pages/me/index' })).resolves.toBe(false)
    await expect(harness.clientSession.ensureClientSession({ redirect: '/pages/me/index' })).resolves.toBe(false)

    expect(harness.router.redirectToLogin).toHaveBeenCalledWith({
      redirect: '/pages/me/index'
    })
    expect(harness.configureHttpSession).toHaveBeenCalledTimes(1)
    expect(harness.fetchCurrentUser).not.toHaveBeenCalled()
  })

  it('refreshes access token with the persisted refresh token', async () => {
    const harness = createHarness()
    harness.sessionStore.saveSessionTokens('old-token', 'refresh-token')

    await expect(harness.clientSession.refreshClientAccessToken()).resolves.toBe('fresh-token')

    expect(harness.configureHttpSession).toHaveBeenCalledTimes(1)
    expect(harness.refreshAccessToken).toHaveBeenCalledWith('refresh-token')
    expect(harness.sessionStore.getAccessToken()).toBe('fresh-token')
    expect(harness.sessionStore.getRefreshToken()).toBe('refresh-token')
  })

  it('does not let a stale login overwrite a newer session', async () => {
    const harness = createHarness()
    harness.fetchCurrentUser.mockImplementation(async () => {
      harness.sessionStore.saveSessionTokens('newer-token', 'newer-refresh')
      return {
        id: 2,
        name: 'Bob'
      }
    })

    await expect(harness.clientSession.loginWithWechat()).rejects.toThrow('登录状态已变化，请重新登录')

    expect(harness.clientSession.getCachedCurrentUser()).toBeNull()
    expect(harness.sessionStore.getAccessToken()).toBe('newer-token')
  })

  it('passes app login context to the injected wechat login api', async () => {
    const harness = createHarness()

    await harness.clientSession.loginWithWechat({ phoneCode: 'phone-code' })

    expect(harness.loginByWechat).toHaveBeenCalledWith('wx-code', {
      phoneCode: 'phone-code'
    })
  })
})
