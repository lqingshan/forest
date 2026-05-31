import { afterEach, describe, expect, it, vi } from 'vitest'
import { clientHttp } from '@forest/http-client'
import { fetchPointLogs } from './api'
import { toPointLogDisplayItems } from './view-model'

describe('point logs api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('queries client point logs with pagination params', async () => {
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue({ content: [] } as never)

    await fetchPointLogs(2, 40)

    expect(getSpy).toHaveBeenCalledWith('/api/client/point/logs/page', {
      params: {
        page: 2,
        size: 40
      }
    })
  })

  it('builds point log display items inside point domain', () => {
    expect(toPointLogDisplayItems([
      {
        id: 1,
        userId: 9,
        direction: 'INCOME',
        amount: 99,
        balanceAfter: 199,
        sourceType: 'RECHARGE',
        sourceId: 3,
        bizKey: 'recharge:3',
        createdTime: '2026-04-18T03:08:00'
      }
    ])).toEqual([
      {
        id: 1,
        amountText: '+99',
        sourceText: '充值到账',
        balanceAfter: 199,
        createdTime: '2026-04-18 03:08',
        direction: 'INCOME'
      }
    ])
  })
})
