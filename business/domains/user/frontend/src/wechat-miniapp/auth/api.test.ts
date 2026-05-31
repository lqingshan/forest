import { afterEach, describe, expect, it, vi } from 'vitest'
import { adminHttp, clientHttp, platformHttp } from '@forest/http-client'
import { createWechatMiniappAuthApi, createWechatMiniappPhoneAuthApi, refreshAccessToken } from './api'

describe('wechat miniapp auth api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('posts login code, client type and app code to direct wechat miniapp login endpoint', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ accessToken: 'token' } as never)
    const authApi = createWechatMiniappAuthApi({
      appCode: 'trade-leads-miniapp'
    })

    await authApi.loginByWechat('wx-code')

    expect(postSpy).toHaveBeenCalledWith('/api/auth/wechat-miniapp/login', {
      code: 'wx-code',
      clientType: 'WECHAT_MINIAPP',
      appCode: 'trade-leads-miniapp',
      accessScope: 'CLIENT'
    }, {
      withAuth: false,
      retryOn401: false
    })
  })

  it('posts login code and phone code to wechat miniapp phone login endpoint', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ accessToken: 'token' } as never)
    const authApi = createWechatMiniappPhoneAuthApi({
      appCode: 'mall-buyer-miniapp'
    })

    await authApi.loginByWechat('wx-code', { phoneCode: 'phone-code' })

    expect(postSpy).toHaveBeenCalledWith('/api/auth/wechat-miniapp/phone-login', {
      code: 'wx-code',
      phoneCode: 'phone-code',
      clientType: 'WECHAT_MINIAPP',
      appCode: 'mall-buyer-miniapp',
      accessScope: 'CLIENT'
    }, {
      withAuth: false,
      retryOn401: false
    })
  })

  it('uses the http client selected by accessScope', async () => {
    const postSpy = vi.spyOn(adminHttp, 'post').mockResolvedValue({ accessToken: 'token' } as never)
    const authApi = createWechatMiniappPhoneAuthApi({
      appCode: 'merchant-miniapp',
      accessScope: 'ADMIN'
    })

    await authApi.loginByWechat('wx-code', { phoneCode: 'phone-code' })

    expect(postSpy).toHaveBeenCalledWith('/api/auth/wechat-miniapp/phone-login', {
      code: 'wx-code',
      phoneCode: 'phone-code',
      clientType: 'WECHAT_MINIAPP',
      appCode: 'merchant-miniapp',
      accessScope: 'ADMIN'
    }, {
      withAuth: false,
      retryOn401: false
    })
  })

  it('rejects phone login before posting when phone code is missing', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ accessToken: 'token' } as never)
    const authApi = createWechatMiniappPhoneAuthApi({
      appCode: 'mall-buyer-miniapp'
    })

    await expect(authApi.loginByWechat('wx-code')).rejects.toThrow('phoneCode 不能为空')
    expect(postSpy).not.toHaveBeenCalled()
  })

  it('posts refresh token to refresh endpoint', async () => {
    const postSpy = vi.spyOn(platformHttp, 'post').mockResolvedValue({ accessToken: 'new-token' } as never)

    await refreshAccessToken('refresh-token', { accessScope: 'PLATFORM' })

    expect(postSpy).toHaveBeenCalledWith('/api/auth/refresh', {
      refreshToken: 'refresh-token'
    }, {
      withAuth: false,
      retryOn401: false
    })
  })
})
