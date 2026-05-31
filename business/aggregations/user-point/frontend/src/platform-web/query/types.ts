import type { User, UserStatus } from '@forest/user/shared/user'
import type { PageResult } from '@forest/api-contracts'

export interface UserPointSummary {
  balance: number
  totalIncome: number
  totalSpend: number
  updatedAt: string | null
}

export interface UserPointRow {
  user: User
  points: UserPointSummary
}

export interface UserPointDetail {
  user: User
  points: UserPointSummary
}

export interface UserPointLog {
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

export interface UserPointPageQuery {
  page?: number
  size?: number
  id?: number
  name?: string
  phone?: string
  email?: string
  status?: UserStatus
}

export type UserPointPage = PageResult<UserPointRow>
export type UserPointLogPage = PageResult<UserPointLog>
