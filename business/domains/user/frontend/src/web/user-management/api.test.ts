import { afterEach, describe, expect, it, vi } from 'vitest'
import { platformHttp } from '@forest/http-client'
import { activateUser, fetchUser, fetchUsers, freezeUser } from './api'

describe('user management api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('builds search query for users', async () => {
    const getSpy = vi.spyOn(platformHttp, 'get').mockResolvedValue({ content: [] } as never)

    await fetchUsers({
      page: 1,
      size: 30,
      id: 9,
      name: 'Lucy',
      phone: '138',
      email: 'lucy@forest.example',
      status: 'FROZEN'
    })

    expect(getSpy).toHaveBeenCalledWith('/api/platform/user/page', {
      params: {
        page: 1,
        size: 30,
        id: 9,
        name: 'Lucy',
        phone: '138',
        email: 'lucy@forest.example',
        status: 'FROZEN'
      }
    })
  })

  it('hits detail, freeze, and activate endpoints with correct methods', async () => {
    const getSpy = vi.spyOn(platformHttp, 'get').mockResolvedValue({ id: 9 } as never)
    const postSpy = vi.spyOn(platformHttp, 'post').mockResolvedValue({ id: 9 } as never)

    await fetchUser(9)
    await freezeUser(9)
    await activateUser(9)

    expect(getSpy).toHaveBeenCalledWith('/api/platform/user/9')
    expect(postSpy).toHaveBeenNthCalledWith(1, '/api/platform/user/9/freeze')
    expect(postSpy).toHaveBeenNthCalledWith(2, '/api/platform/user/9/activate')
  })
})
