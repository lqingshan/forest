import { afterEach, describe, expect, it, vi } from 'vitest'
import { clientHttp } from '@forest/http-client'
import { fetchUserLeadList } from './api'
import { toUserLeadListCardModels } from './view-model'

describe('user leads list api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('builds aggregated lead list query for miniapp', async () => {
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue({ content: [] } as never)

    await fetchUserLeadList({ page: 2, size: 12, keyword: 'chair', country: 'CN' })

    expect(getSpy).toHaveBeenCalledWith('/api/client/user-lead/page', {
      params: {
        page: 2,
        size: 12,
        keyword: 'chair',
        country: 'CN'
      }
    })
  })

  it('builds list card display models inside user-lead aggregation', () => {
    expect(toUserLeadListCardModels([
      {
        id: 1,
        name: 'Forest Buyer',
        category: null,
        country: null,
        unlocked: false,
        phone: '',
        website: ''
      }
    ])).toEqual([
      {
        id: 1,
        name: 'Forest Buyer',
        category: '未分类',
        country: '未知地区',
        unlocked: false,
        phone: '',
        website: '',
        statusText: '待解锁'
      }
    ])
  })
})
