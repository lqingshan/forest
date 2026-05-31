import { afterEach, describe, expect, it, vi } from 'vitest'
import { platformHttp } from '@forest/http-client'
import { fetchUserPointDetail, fetchUserPointLogs, fetchUserPointPage } from './api'

describe('user points api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('queries aggregated user points page', async () => {
    const getSpy = vi.spyOn(platformHttp, 'get').mockResolvedValue({ content: [] } as never)

    await fetchUserPointPage({ page: 1, size: 20, name: 'forest' })

    expect(getSpy).toHaveBeenCalledWith('/api/platform/user-point/page', {
      params: {
        page: 1,
        size: 20,
        name: 'forest'
      }
    })
  })

  it('queries aggregated user point detail', async () => {
    const getSpy = vi.spyOn(platformHttp, 'get').mockResolvedValue({
      user: { id: 8 },
      points: { balance: 0 }
    } as never)

    await fetchUserPointDetail(8)

    expect(getSpy).toHaveBeenCalledWith('/api/platform/user-point/8')
  })

  it('queries aggregated user point logs', async () => {
    const getSpy = vi.spyOn(platformHttp, 'get').mockResolvedValue({ content: [] } as never)

    await fetchUserPointLogs(8, 2, 40)

    expect(getSpy).toHaveBeenCalledWith('/api/platform/user-point/8/logs/page', {
      params: {
        page: 2,
        size: 40
      }
    })
  })
})
