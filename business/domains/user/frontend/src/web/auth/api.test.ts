import { afterEach, describe, expect, it, vi } from 'vitest'
import { adminHttp, clientHttp, platformHttp } from '@forest/http-client'
import { loginByCarrierToken, loginByPassword, loginByPhone, logout } from './api'

describe('web phone sms auth api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('posts phone and sms code to generic phone login endpoint', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ accessToken: 'token' } as never)

    await loginByPhone('13800138000', '121314', {
      appCode: 'cxc-commerce-buyer-mobile-h5',
      clientType: 'MOBILE_H5',
      accessScope: 'CLIENT'
    })

    expect(postSpy).toHaveBeenCalledWith('/api/auth/phone/login', {
      phone: '13800138000',
      smsCode: '121314',
      clientType: 'MOBILE_H5',
      appCode: 'cxc-commerce-buyer-mobile-h5',
      accessScope: 'CLIENT'
    }, {
      withAuth: false,
      retryOn401: false
    })
  })

  it('rejects blank app code before posting login request', () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ accessToken: 'token' } as never)

    expect(() => loginByPhone('13800138000', '121314', {
      appCode: ' ',
      clientType: 'MOBILE_H5',
      accessScope: 'CLIENT'
    })).toThrow('用户端认证 appCode 未配置')
    expect(postSpy).not.toHaveBeenCalled()
  })

  it('posts phone and password to generic password login endpoint', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ accessToken: 'token' } as never)

    await loginByPassword('13800138000', 'secret', {
      appCode: 'cxc-commerce-merchant-web',
      clientType: 'PC_WEB',
      accessScope: 'ADMIN'
    })

    expect(postSpy).toHaveBeenCalledWith('/api/auth/password/login', {
      phone: '13800138000',
      password: 'secret',
      clientType: 'PC_WEB',
      appCode: 'cxc-commerce-merchant-web',
      accessScope: 'ADMIN'
    }, {
      withAuth: false,
      retryOn401: false
    })
  })

  it('supports injecting the scoped http client for password login', async () => {
    const postSpy = vi.spyOn(adminHttp, 'post').mockResolvedValue({ accessToken: 'token' } as never)

    await loginByPassword('13800138000', 'secret', {
      appCode: 'cxc-commerce-merchant-web',
      clientType: 'PC_WEB',
      accessScope: 'ADMIN'
    }, adminHttp)

    expect(postSpy).toHaveBeenCalledWith('/api/auth/password/login', {
      phone: '13800138000',
      password: 'secret',
      clientType: 'PC_WEB',
      appCode: 'cxc-commerce-merchant-web',
      accessScope: 'ADMIN'
    }, {
      withAuth: false,
      retryOn401: false
    })
  })

  it('posts carrier token to native one-click-login endpoint', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ accessToken: 'token' } as never)

    await loginByCarrierToken('carrier-token', {
      provider: 'ALIYUN',
      appCode: 'cxc-commerce-buyer-android',
      clientType: 'ANDROID_APP',
      accessScope: 'CLIENT'
    })

    expect(postSpy).toHaveBeenCalledWith('/api/auth/carrier/one-click-login', {
      carrierToken: 'carrier-token',
      provider: 'ALIYUN',
      clientType: 'ANDROID_APP',
      appCode: 'cxc-commerce-buyer-android',
      accessScope: 'CLIENT'
    }, {
      withAuth: false,
      retryOn401: false
    })
  })

  it('uses the injected scoped http client for logout', async () => {
    const postSpy = vi.spyOn(platformHttp, 'post').mockResolvedValue({ success: true } as never)

    await logout(platformHttp)

    expect(postSpy).toHaveBeenCalledWith('/api/auth/logout')
  })
})
