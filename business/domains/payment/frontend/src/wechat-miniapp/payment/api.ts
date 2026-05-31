import { apiPaths, clientHttp } from '@forest/http-client'
import type { MockPaymentNotifyPayload, PaymentOrder } from './types'

const API_BASE = `${apiPaths.client}/payment`
const OPEN_WECHAT_PAY_NOTIFY_API = `${apiPaths.open}/wechat/pay/notify`

// 创建支付单会返回小程序收银台所需参数，但最终到账以后端微信回调为准。
export function createPaymentOrder(bizType: string, bizOrderId: number) {
  return clientHttp.post<PaymentOrder>(`${API_BASE}/orders`, { bizType, bizOrderId })
}

export function fetchPaymentOrder(id: number) {
  return clientHttp.get<PaymentOrder>(`${API_BASE}/orders/${id}`)
}

// 仅本地 mock 支付使用；真实微信支付由微信服务器主动回调同一个 open API。
export function notifyMockPayment(payload: MockPaymentNotifyPayload) {
  return clientHttp.post<string>(OPEN_WECHAT_PAY_NOTIFY_API, payload, {
    withAuth: false,
    retryOn401: false
  })
}
