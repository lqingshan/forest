import { apiPaths, platformHttp } from '@forest/http-client'
import type { LeadPlatformActionResult, LeadPlatformItem, LeadPlatformDraft, LeadPlatformPage, LeadPlatformPageQuery } from './types'

const API_BASE = `${apiPaths.platform}/lead`
const PAGE_API = `${API_BASE}/page`

export function fetchLeadPlatformItems(query: LeadPlatformPageQuery = {}) {
  return platformHttp.get<LeadPlatformPage>(PAGE_API, {
    params: query
  })
}

export function fetchLeadPlatformItem(id: number) {
  return platformHttp.get<LeadPlatformItem>(`${API_BASE}/${id}`)
}

export function createLeadPlatformItem(payload: LeadPlatformDraft) {
  return platformHttp.post<LeadPlatformItem>(API_BASE, payload)
}

export function updateLeadPlatformItem(id: number, payload: LeadPlatformDraft) {
  return platformHttp.put<LeadPlatformItem>(`${API_BASE}/${id}`, payload)
}

export function deleteLeadPlatformItem(id: number) {
  return platformHttp.delete<LeadPlatformActionResult>(`${API_BASE}/${id}`)
}
