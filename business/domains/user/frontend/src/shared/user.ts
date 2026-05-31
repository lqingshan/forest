export type UserStatus = 'ACTIVE' | 'FROZEN' | 'DISABLED'

export interface User {
  id?: number
  userId?: number
  loginName?: string
  name: string | null
  avatar: string | null
  avatarUrl: string | null
  phone: string | null
  email: string | null
  status: UserStatus
  adminUser?: boolean
  user?: boolean
}
