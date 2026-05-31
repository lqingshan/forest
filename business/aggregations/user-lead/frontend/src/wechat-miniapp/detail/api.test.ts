import { afterEach, describe, expect, it, vi } from 'vitest'
import { clientHttp } from '@forest/http-client'
import { fetchUserLeadDetail, unlockUserLead } from './api'
import { toLeadDetailCardModel } from './view-model'

describe('user leads detail api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('queries aggregated lead detail', async () => {
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue({ id: 8 } as never)

    await fetchUserLeadDetail(8)

    expect(getSpy).toHaveBeenCalledWith('/api/client/user-lead/8')
  })

  it('posts aggregated unlock request', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ success: true } as never)

    await unlockUserLead(8)

    expect(postSpy).toHaveBeenCalledWith('/api/client/user-lead/8/unlock')
  })

  it('builds detail display model inside user-lead aggregation', () => {
    expect(toLeadDetailCardModel({
      id: 8,
      name: 'Detail Buyer',
      category: null,
      country: null,
      intro: null,
      unlocked: false,
      phone: '已遮挡，解锁后可见',
      email: '已遮挡，解锁后可见',
      website: '已遮挡，解锁后可见'
    })).toMatchObject({
      categoryText: '未分类',
      countryText: '未知地区',
      introText: '暂无采购信息补充说明。'
    })
  })
})
