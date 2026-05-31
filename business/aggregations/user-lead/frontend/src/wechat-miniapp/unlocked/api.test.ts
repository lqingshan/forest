import { afterEach, describe, expect, it, vi } from 'vitest'
import { clientHttp } from '@forest/http-client'
import { fetchUnlockedUserLeadList } from './api'
import { toUnlockedLeadCardModels } from './view-model'

describe('unlocked user leads api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('queries unlocked user leads with pagination params', async () => {
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue({ content: [] } as never)

    await fetchUnlockedUserLeadList(1, 8)

    expect(getSpy).toHaveBeenCalledWith('/api/client/user-lead/unlocked/page', {
      params: {
        page: 1,
        size: 8
      }
    })
  })

  it('builds unlocked lead card display models inside user-lead aggregation', () => {
    expect(toUnlockedLeadCardModels([
      {
        id: 2,
        name: 'Unlocked Buyer',
        category: null,
        country: 'CN',
        unlocked: true,
        phone: '13800138000',
        website: ''
      }
    ])).toEqual([
      {
        id: 2,
        name: 'Unlocked Buyer',
        category: '未分类',
        country: 'CN',
        phone: '13800138000',
        website: ''
      }
    ])
  })
})
