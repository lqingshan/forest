import { afterEach, describe, expect, it, vi } from 'vitest'
import { platformHttp } from '@forest/http-client'
import { createLeadPlatformItem, deleteLeadPlatformItem, fetchLeadPlatformItem, fetchLeadPlatformItems, updateLeadPlatformItem } from './api'

describe('platform lead management api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('builds platform lead query parameters', async () => {
    const getSpy = vi.spyOn(platformHttp, 'get').mockResolvedValue({ content: [] } as never)

    await fetchLeadPlatformItems({ page: 1, size: 10, keyword: 'hotel', country: 'US' })

    expect(getSpy).toHaveBeenCalledWith('/api/platform/lead/page', {
      params: {
        page: 1,
        size: 10,
        keyword: 'hotel',
        country: 'US'
      }
    })
  })

  it('uses the correct endpoints and methods for CRUD actions', async () => {
    const getSpy = vi.spyOn(platformHttp, 'get').mockResolvedValue({ id: 6 } as never)
    const postSpy = vi.spyOn(platformHttp, 'post').mockResolvedValue({ id: 6 } as never)
    const putSpy = vi.spyOn(platformHttp, 'put').mockResolvedValue({ id: 6 } as never)
    const deleteSpy = vi.spyOn(platformHttp, 'delete').mockResolvedValue({ success: true } as never)
    const payload = { name: 'North Harbor' }

    await fetchLeadPlatformItem(6)
    await createLeadPlatformItem(payload)
    await updateLeadPlatformItem(6, payload)
    await deleteLeadPlatformItem(6)

    expect(getSpy).toHaveBeenCalledWith('/api/platform/lead/6')
    expect(postSpy).toHaveBeenCalledWith('/api/platform/lead', payload)
    expect(putSpy).toHaveBeenCalledWith('/api/platform/lead/6', payload)
    expect(deleteSpy).toHaveBeenCalledWith('/api/platform/lead/6')
  })
})
