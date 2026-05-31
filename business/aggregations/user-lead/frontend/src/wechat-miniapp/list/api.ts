import { apiPaths, clientHttp } from '@forest/http-client'
import type { UserLeadListPage, UserLeadListQuery } from './types'

const API_BASE = `${apiPaths.client}/user-lead`

// 小程序线索列表读取聚合接口，联系方式遮罩和解锁状态都由后端 user-lead 聚合层决定。
export function fetchUserLeadList(query: UserLeadListQuery = {}) {
  return clientHttp.get<UserLeadListPage>(`${API_BASE}/page`, {
    params: query
  })
}
