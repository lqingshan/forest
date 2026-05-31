export interface RechargePackage {
  code: string
  title: string
  amountCents: number
  creditedPoints: number
}

export interface RechargeOrder {
  id: number
  rechargeNo: string
  packageCode: string
  amountCents: number
  creditedPoints: number
  status: 'CREATED' | 'PAID' | 'CLOSED'
  paidPaymentOrderId: number | null
  createdTime: string
  paidTime: string | null
}
