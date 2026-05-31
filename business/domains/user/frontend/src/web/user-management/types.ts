import type { PageResult } from '@forest/api-contracts'
import type { User, UserStatus } from '../../shared/user'

export type { User, UserStatus } from '../../shared/user'

export type UserManagementUser = User & {
  createdTime: string | null
}

export interface UserPageQuery {
  page?: number
  size?: number
  id?: number
  name?: string
  phone?: string
  email?: string
  status?: UserStatus
}

export type UserPage = PageResult<UserManagementUser>
