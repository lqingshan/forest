import { apiPaths, platformHttp } from '@forest/http-client'
import type { UserPointDetail, UserPointLogPage, UserPointPage, UserPointPageQuery } from './types'

const API_BASE = `${apiPaths.platform}/user-point`

export function fetchUserPointPage(query: UserPointPageQuery = {}) {
  return platformHttp.get<UserPointPage>(`${API_BASE}/page`, {
    params: query
  })
}

export function fetchUserPointDetail(userId: number) {
  return platformHttp.get<UserPointDetail>(`${API_BASE}/${userId}`)
}

export function fetchUserPointLogs(userId: number, page = 0, size = 20) {
  return platformHttp.get<UserPointLogPage>(`${API_BASE}/${userId}/logs/page`, {
    params: { page, size }
  })
}
