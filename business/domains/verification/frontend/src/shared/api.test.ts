import { afterEach, describe, expect, it, vi } from 'vitest'
import { clientHttp } from '@forest/http-client'
import { normalizePhone, normalizeSmsCode, sendSmsCode } from './index'

describe('verification shared api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('posts sms code send request without auth', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ phone: '13800138000', ttlMinutes: 5 } as never)

    await sendSmsCode({
      phone: ' 138 0013 8000 ',
      clientType: 'MOBILE_H5',
      appCode: 'cxc-commerce-buyer-mobile-h5',
      accessScope: 'CLIENT'
    })

    expect(postSpy).toHaveBeenCalledWith('/api/auth/sms/send', {
      phone: '13800138000',
      clientType: 'MOBILE_H5',
      appCode: 'cxc-commerce-buyer-mobile-h5',
      accessScope: 'CLIENT'
    }, {
      withAuth: false,
      retryOn401: false
    })
  })

  it('normalizes phone and sms code input', () => {
    expect(normalizePhone(' 138 0013 8000 ')).toBe('13800138000')
    expect(normalizeSmsCode('12a34 567', 6)).toBe('123456')
  })
})
