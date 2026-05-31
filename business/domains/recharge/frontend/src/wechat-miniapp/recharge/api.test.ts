import { afterEach, describe, expect, it, vi } from 'vitest'
import { clientHttp } from '@forest/http-client'
import { createRechargeOrder, fetchRechargeOrder, fetchRechargePackages } from './api'
import { resolveRechargePaymentResult } from './payment-result-resolver'
import type { RechargeOrder } from './types'
import {
  buildRechargeResultMeta,
  formatRechargeAmount,
  pickDefaultRechargePackageCode,
  toRechargePackageOptions
} from './view-model'

function createRechargeOrderFixture(overrides: Partial<RechargeOrder> = {}): RechargeOrder {
  return {
    id: 1,
    rechargeNo: 'RECHARGE-1',
    packageCode: 'starter',
    amountCents: 1,
    creditedPoints: 99,
    status: 'CREATED',
    paidPaymentOrderId: null,
    createdTime: '2026-04-18T03:00:00',
    paidTime: null,
    ...overrides
  }
}

describe('recharge miniapp api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('loads recharge packages', async () => {
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue([] as never)

    await fetchRechargePackages()

    expect(getSpy).toHaveBeenCalledWith('/api/client/recharge/packages')
  })

  it('creates recharge order with package code', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ id: 1 } as never)

    await createRechargeOrder('starter')

    expect(postSpy).toHaveBeenCalledWith('/api/client/recharge/orders', {
      packageCode: 'starter'
    })
  })

  it('queries recharge order by id', async () => {
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue({ id: 3 } as never)

    await fetchRechargeOrder(3)

    expect(getSpy).toHaveBeenCalledWith('/api/client/recharge/orders/3')
  })

  it('builds miniapp package display options inside recharge domain', () => {
    const options = toRechargePackageOptions([
      {
        code: 'starter',
        title: '入门包',
        amountCents: 1,
        creditedPoints: 99
      }
    ])

    expect(options).toEqual([
      {
        code: 'starter',
        title: '入门包',
        amountCents: 1,
        creditedPoints: 99,
        amountText: '￥0.01',
        creditedPointsText: '到账 99 积分'
      }
    ])
    expect(pickDefaultRechargePackageCode(options)).toBe('starter')
    expect(pickDefaultRechargePackageCode([])).toBe('')
    expect(formatRechargeAmount(0)).toBe('￥0.00')
  })

  it('builds recharge result status copy inside recharge domain', () => {
    expect(buildRechargeResultMeta('success', {
      id: 1,
      rechargeNo: 'RECHARGE-1',
      packageCode: 'starter',
      amountCents: 1,
      creditedPoints: 99,
      status: 'PAID',
      paidPaymentOrderId: 3,
      createdTime: '2026-04-18T03:00:00',
      paidTime: '2026-04-18T03:01:00'
    })).toEqual({
      title: '支付成功',
      description: '充值单已到账，获得 99 积分。'
    })
    expect(buildRechargeResultMeta('cancelled', null).title).toBe('支付已取消')
    expect(buildRechargeResultMeta('processing', null).title).toBe('支付处理中')
  })

  it('resolves payment result as success when recharge order is already paid', async () => {
    const wait = vi.fn().mockResolvedValue(undefined)
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue(createRechargeOrderFixture({
      status: 'PAID',
      paidPaymentOrderId: 3,
      paidTime: '2026-04-18T03:01:00'
    }) as never)

    const result = await resolveRechargePaymentResult({
      rechargeOrderId: 9,
      initialStatus: 'processing',
      wait
    })

    expect(getSpy).toHaveBeenCalledWith('/api/client/recharge/orders/9')
    expect(getSpy).toHaveBeenCalledTimes(1)
    expect(wait).not.toHaveBeenCalled()
    expect(result.status).toBe('success')
    expect(result.order?.status).toBe('PAID')
  })

  it('keeps polling until recharge order becomes paid', async () => {
    const wait = vi.fn().mockResolvedValue(undefined)
    const getSpy = vi.spyOn(clientHttp, 'get')
      .mockResolvedValueOnce(createRechargeOrderFixture({ status: 'CREATED' }) as never)
      .mockResolvedValueOnce(createRechargeOrderFixture({
        status: 'PAID',
        paidPaymentOrderId: 3,
        paidTime: '2026-04-18T03:01:00'
      }) as never)

    const result = await resolveRechargePaymentResult({
      rechargeOrderId: 10,
      initialStatus: 'processing',
      wait
    })

    expect(getSpy).toHaveBeenCalledTimes(2)
    expect(wait).toHaveBeenCalledTimes(1)
    expect(wait).toHaveBeenCalledWith(900)
    expect(result.status).toBe('success')
  })

  it('returns processing with the last order when recharge order is not paid after retries', async () => {
    const wait = vi.fn().mockResolvedValue(undefined)
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue(createRechargeOrderFixture({
      status: 'CREATED'
    }) as never)

    const result = await resolveRechargePaymentResult({
      rechargeOrderId: 11,
      initialStatus: 'processing',
      wait
    })

    expect(getSpy).toHaveBeenCalledTimes(5)
    expect(wait).toHaveBeenCalledTimes(5)
    expect(result.status).toBe('processing')
    expect(result.order?.status).toBe('CREATED')
  })

  it('keeps cancelled status when recharge order is not paid', async () => {
    vi.spyOn(clientHttp, 'get').mockResolvedValue(createRechargeOrderFixture({
      status: 'CREATED'
    }) as never)

    const result = await resolveRechargePaymentResult({
      rechargeOrderId: 12,
      initialStatus: 'cancelled',
      maxAttempts: 1,
      wait: vi.fn().mockResolvedValue(undefined)
    })

    expect(result.status).toBe('cancelled')
  })

  it('downgrades optimistic success to processing before backend confirms paid', async () => {
    vi.spyOn(clientHttp, 'get').mockResolvedValue(createRechargeOrderFixture({
      status: 'CREATED'
    }) as never)

    const result = await resolveRechargePaymentResult({
      rechargeOrderId: 13,
      initialStatus: 'success',
      maxAttempts: 1,
      wait: vi.fn().mockResolvedValue(undefined)
    })

    expect(result.status).toBe('processing')
  })
})
