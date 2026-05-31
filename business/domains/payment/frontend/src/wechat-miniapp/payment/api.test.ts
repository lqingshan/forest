import { afterEach, describe, expect, it, vi } from 'vitest'
import { clientHttp } from '@forest/http-client'
import { createPaymentOrder, fetchPaymentOrder, notifyMockPayment } from './api'

describe('payment miniapp api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('creates payment order from business order', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ id: 1 } as never)

    await createPaymentOrder('RECHARGE', 9)

    expect(postSpy).toHaveBeenCalledWith('/api/client/payment/orders', {
      bizType: 'RECHARGE',
      bizOrderId: 9
    })
  })

  it('queries payment order by id', async () => {
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue({ id: 5 } as never)

    await fetchPaymentOrder(5)

    expect(getSpy).toHaveBeenCalledWith('/api/client/payment/orders/5')
  })

  it('notifies mock payment through open callback api without auth', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue('success' as never)

    await notifyMockPayment({
      outTradeNo: 'OUT-001',
      transactionId: 'mock-tx-1',
      amountCents: 1990
    })

    expect(postSpy).toHaveBeenCalledWith('/api/open/wechat/pay/notify', {
      outTradeNo: 'OUT-001',
      transactionId: 'mock-tx-1',
      amountCents: 1990
    }, {
      withAuth: false,
      retryOn401: false
    })
  })
})
