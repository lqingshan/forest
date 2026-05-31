export interface PointBalanceCardModel {
  userId: number
  balance: number
  totalIncome: number
  totalSpend: number
}

export interface PointLogItem {
  id: number
  userId: number
  direction: 'INCOME' | 'SPEND'
  amount: number
  balanceAfter: number
  sourceType: 'RECHARGE' | 'UNLOCK'
  sourceId: number | null
  bizKey: string
  createdTime: string
}
