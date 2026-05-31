import { apiPaths, clientHttp } from '@forest/http-client'
import type { UserLeadListPage } from '../list/types'

const API_BASE = `${apiPaths.client}/user-lead`

// 我的已解锁线索只读取历史解锁结果，不触发新的解锁扣费。
export function fetchUnlockedUserLeadList(page = 0, size = 20) {
  return clientHttp.get<UserLeadListPage>(`${API_BASE}/unlocked/page`, {
    params: { page, size }
  })
}
