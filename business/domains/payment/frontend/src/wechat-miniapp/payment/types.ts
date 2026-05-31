export interface WechatMiniappPaymentParams {
  timeStamp: string
  nonceStr: string
  packageValue: string
  signType: string
  paySign: string
}

export interface PaymentOrder {
  id: number
  paymentNo: string
  bizType: string
  bizOrderId: number
  channel: 'WECHAT_MINIAPP_PAYMENT'
  amountCents: number
  status: 'CREATED' | 'PREPAY_CREATED' | 'SUCCESS' | 'FAILED' | 'CLOSED'
  outTradeNo: string
  paymentParams: WechatMiniappPaymentParams | null
  paidTime: string | null
}

export interface MockPaymentNotifyPayload {
  outTradeNo: string
  transactionId: string
  amountCents: number
  paidTime?: string
}
