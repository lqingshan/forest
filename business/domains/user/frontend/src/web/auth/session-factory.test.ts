import { afterEach, describe, expect, it, vi } from 'vitest'
import { adminHttp, clientHttp, platformHttp } from '@forest/http-client'
import { createWebUserSession } from './session-factory'
import type { LoginResult } from './types'

const user = {
  id: 1,
  name: '用户',
  avatar: null,
  avatarUrl: null,
  phone: '13800138000',
  email: null,
  status: 'ACTIVE',
  adminUser: false,
  user: true
} as const

describe('createWebUserSession', () => {
  afterEach(() => {
    window.localStorage.clear()
    vi.restoreAllMocks()
  })

  it('stores tokens using storage prefix and refreshes current user after login', async () => {
    vi.spyOn(adminHttp, 'get').mockResolvedValue(user as never)
    const session = createWebUserSession({
      appCode: 'cxc-commerce-merchant-web',
      clientType: 'PC_WEB',
      accessScope: 'ADMIN',
      storagePrefix: 'forest.cxc-commerce.merchant'
    })

    await session.applyLoginResult(loginResult('ADMIN'))

    expect(window.localStorage.getItem('forest.cxc-commerce.merchant.accessToken')).toBe('access-token')
    expect(window.localStorage.getItem('forest.cxc-commerce.merchant.refreshToken')).toBe('refresh-token')
    expect(adminHttp.get).toHaveBeenCalledWith('/api/admin/user/me')
    expect(session.state.currentUser).toEqual(user)
  })

  it('clears tokens when current user fetch fails after login', async () => {
    vi.spyOn(platformHttp, 'get').mockRejectedValue(new Error('forbidden'))
    const session = createWebUserSession({
      appCode: 'trade-leads-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.trade-leads.platform'
    })

    await expect(session.applyLoginResult(loginResult('PLATFORM'))).rejects.toThrow('forbidden')

    expect(window.localStorage.getItem('forest.trade-leads.platform.accessToken')).toBeNull()
    expect(window.localStorage.getItem('forest.trade-leads.platform.refreshToken')).toBeNull()
    expect(session.state.currentUser).toBeNull()
  })

  it('derives current user endpoint from access scope', async () => {
    vi.spyOn(clientHttp, 'get').mockResolvedValue(user as never)
    vi.spyOn(platformHttp, 'get').mockResolvedValue(user as never)

    const clientSession = createWebUserSession({
      appCode: 'client-app',
      clientType: 'MOBILE_H5',
      accessScope: 'CLIENT'
    })
    const platformSession = createWebUserSession({
      appCode: 'platform-app',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM'
    })

    await clientSession.fetchCurrentUser()
    await platformSession.fetchCurrentUser()

    expect(clientHttp.get).toHaveBeenCalledWith('/api/client/user/me')
    expect(platformHttp.get).toHaveBeenCalledWith('/api/platform/user/me')
  })

  it('records phone password login history after successful login', async () => {
    vi.spyOn(platformHttp, 'post').mockResolvedValue(loginResult('PLATFORM') as never)
    vi.spyOn(platformHttp, 'get').mockResolvedValue(user as never)
    const session = createWebUserSession({
      appCode: 'trade-leads-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.trade-leads.platform'
    })

    await session.loginWithPassword(' 13800138000 ', 'secret')

    expect(session.getLoginHistory()).toEqual({
      records: [
        { mode: 'phone_password', identifier: '13800138000', password: 'secret' }
      ]
    })
    expect(window.localStorage.getItem('forest.trade-leads.platform.loginHistory')).toBe(JSON.stringify({
      records: [
        { mode: 'phone_password', identifier: '13800138000', password: 'secret' }
      ]
    }))
  })

  it('records phone password login history without password when savePassword is false', async () => {
    vi.spyOn(platformHttp, 'post').mockResolvedValue(loginResult('PLATFORM') as never)
    vi.spyOn(platformHttp, 'get').mockResolvedValue(user as never)
    const session = createWebUserSession({
      appCode: 'trade-leads-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.trade-leads.platform'
    })

    await session.loginWithPassword('13800138000', 'old-secret')
    await session.loginWithPassword('13800138000', 'new-secret', { savePassword: false })

    expect(session.getLoginHistory()).toEqual({
      records: [
        { mode: 'phone_password', identifier: '13800138000' }
      ]
    })
  })

  it('deduplicates phone password login history by mode and identifier', async () => {
    vi.spyOn(platformHttp, 'post').mockResolvedValue(loginResult('PLATFORM') as never)
    vi.spyOn(platformHttp, 'get').mockResolvedValue(user as never)
    const session = createWebUserSession({
      appCode: 'trade-leads-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.trade-leads.platform'
    })

    await session.loginWithPassword('13800138000', 'old-secret')
    await session.loginWithPassword('13800138000', 'new-secret')

    expect(session.getLoginHistory()).toEqual({
      records: [
        { mode: 'phone_password', identifier: '13800138000', password: 'new-secret' }
      ]
    })
  })

  it('records phone sms login history without sms code after successful login', async () => {
    vi.spyOn(platformHttp, 'post').mockResolvedValue(loginResult('PLATFORM') as never)
    vi.spyOn(platformHttp, 'get').mockResolvedValue(user as never)
    const session = createWebUserSession({
      appCode: 'trade-leads-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.trade-leads.platform'
    })

    await session.loginWithPhoneSms(' 13800138000 ', '121314')

    expect(session.getLoginHistory()).toEqual({
      records: [
        { mode: 'phone_sms', identifier: '13800138000' }
      ]
    })
  })

  it('does not record login history when login request fails', async () => {
    vi.spyOn(platformHttp, 'post').mockRejectedValue(new Error('bad credentials'))
    vi.spyOn(platformHttp, 'get').mockResolvedValue(user as never)
    const session = createWebUserSession({
      appCode: 'trade-leads-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.trade-leads.platform'
    })

    await expect(session.loginWithPassword('13800138000', 'secret')).rejects.toThrow('bad credentials')

    expect(session.getLoginHistory()).toEqual({ records: [] })
  })

  it('does not record login history when current user fetch fails after login', async () => {
    vi.spyOn(platformHttp, 'post').mockResolvedValue(loginResult('PLATFORM') as never)
    vi.spyOn(platformHttp, 'get').mockRejectedValue(new Error('forbidden'))
    const session = createWebUserSession({
      appCode: 'trade-leads-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.trade-leads.platform'
    })

    await expect(session.loginWithPassword('13800138000', 'secret')).rejects.toThrow('forbidden')

    expect(session.getLoginHistory()).toEqual({ records: [] })
  })

  it('isolates login history by storage prefix', async () => {
    vi.spyOn(platformHttp, 'post').mockResolvedValue(loginResult('PLATFORM') as never)
    vi.spyOn(platformHttp, 'get').mockResolvedValue(user as never)
    const tradeLeadsSession = createWebUserSession({
      appCode: 'trade-leads-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.trade-leads.platform'
    })
    const commerceSession = createWebUserSession({
      appCode: 'cxc-commerce-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.cxc-commerce.platform'
    })

    await tradeLeadsSession.loginWithPassword('13800138000', 'trade-secret')
    await commerceSession.loginWithPassword('13800138000', 'commerce-secret')

    expect(tradeLeadsSession.getLoginHistory()).toEqual({
      records: [
        { mode: 'phone_password', identifier: '13800138000', password: 'trade-secret' }
      ]
    })
    expect(commerceSession.getLoginHistory()).toEqual({
      records: [
        { mode: 'phone_password', identifier: '13800138000', password: 'commerce-secret' }
      ]
    })
  })

  it('logs out with scoped http client and clears tokens even when request fails', async () => {
    vi.spyOn(platformHttp, 'post').mockRejectedValue(new Error('network down'))
    const session = createWebUserSession({
      appCode: 'trade-leads-platform-web',
      clientType: 'PC_WEB',
      accessScope: 'PLATFORM',
      storagePrefix: 'forest.trade-leads.platform'
    })

    window.localStorage.setItem('forest.trade-leads.platform.accessToken', 'access-token')
    window.localStorage.setItem('forest.trade-leads.platform.refreshToken', 'refresh-token')

    await expect(session.logout()).resolves.toBeUndefined()

    expect(platformHttp.post).toHaveBeenCalledWith('/api/auth/logout')
    expect(window.localStorage.getItem('forest.trade-leads.platform.accessToken')).toBeNull()
    expect(window.localStorage.getItem('forest.trade-leads.platform.refreshToken')).toBeNull()
  })
})

function loginResult(accessScope: string): LoginResult {
  return {
    accessToken: 'access-token',
    refreshToken: 'refresh-token',
    tokenType: 'Bearer',
    expiresIn: 7200,
    refreshExpiresIn: 1209600,
    clientType: 'PC_WEB',
    appCode: 'app',
    accessScope,
    firstLogin: false
  }
}
