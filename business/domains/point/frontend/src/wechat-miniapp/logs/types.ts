import type { PageResult } from '@forest/api-contracts'

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

export type PointLogPage = PageResult<PointLogItem>
