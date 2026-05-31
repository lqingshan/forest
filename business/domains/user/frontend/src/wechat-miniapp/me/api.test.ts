import { afterEach, describe, expect, it, vi } from 'vitest'
import { adminHttp, clientHttp, platformHttp } from '@forest/http-client'
import { createWechatMiniappMeApi, fetchCurrentUser } from './api'

describe('wechat miniapp me api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('fetches the shared current user shape', async () => {
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue({
      id: 9,
      name: 'Alice',
      avatar: null,
      avatarUrl: null,
      phone: null,
      email: null,
      status: 'ACTIVE',
      adminUser: true,
      user: true
    } as never)

    const result = await fetchCurrentUser()

    expect(getSpy).toHaveBeenCalledWith('/api/client/user/me')
    expect(result).toMatchObject({
      id: 9,
      adminUser: true,
      user: true
    })
  })

  it('selects current user endpoint by accessScope', async () => {
    vi.spyOn(adminHttp, 'get').mockResolvedValue({ id: 10 } as never)
    vi.spyOn(platformHttp, 'get').mockResolvedValue({ id: 11 } as never)

    await createWechatMiniappMeApi({ accessScope: 'ADMIN' }).fetchCurrentUser()
    await createWechatMiniappMeApi({ accessScope: 'PLATFORM' }).fetchCurrentUser()

    expect(adminHttp.get).toHaveBeenCalledWith('/api/admin/user/me')
    expect(platformHttp.get).toHaveBeenCalledWith('/api/platform/user/me')
  })
})
