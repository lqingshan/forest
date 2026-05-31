export interface UserLeadDetail {
  id: number
  name: string
  category: string | null
  country: string | null
  intro: string | null
  unlocked: boolean
  phone: string
  email: string
  website: string
}

export interface UserLeadUnlockResult {
  success: boolean
  message: string
  leadId: number
  balanceAfter: number
}
