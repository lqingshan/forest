import type { PageResult } from '@forest/api-contracts'

export interface LeadPlatformItem {
  id: number
  sourceType: string | null
  keywords: string | null
  name: string
  category: string | null
  country: string | null
  phone: string | null
  email: string | null
  website: string | null
  intro: string | null
  createdTime?: string | null
  modifiedTime?: string | null
}

export interface LeadPlatformPageQuery {
  page?: number
  size?: number
  keyword?: string
  country?: string
}

export interface LeadPlatformDraft {
  sourceType?: string | null
  keywords?: string | null
  name: string
  category?: string | null
  country?: string | null
  phone?: string | null
  email?: string | null
  website?: string | null
  intro?: string | null
}

export interface LeadPlatformActionResult {
  success: boolean
}

export type LeadPlatformPage = PageResult<LeadPlatformItem>
