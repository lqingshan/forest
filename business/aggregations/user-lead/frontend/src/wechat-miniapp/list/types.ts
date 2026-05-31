import type { PageResult } from '@forest/api-contracts'

export interface UserLeadListItem {
  id: number
  name: string
  category: string | null
  country: string | null
  unlocked: boolean
  phone: string
  website: string
}

export interface UserLeadListQuery {
  page?: number
  size?: number
  keyword?: string
  country?: string
}

export type UserLeadListPage = PageResult<UserLeadListItem>
