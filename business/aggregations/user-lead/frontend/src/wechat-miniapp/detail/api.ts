import { apiPaths, clientHttp } from '@forest/http-client'
import type { UserLeadDetail, UserLeadUnlockResult } from './types'

const API_BASE = `${apiPaths.client}/user-lead`

// 详情接口返回同一份遮罩策略结果，前端不直接拼接敏感联系方式。
export function fetchUserLeadDetail(id: number) {
  return clientHttp.get<UserLeadDetail>(`${API_BASE}/${id}`)
}

// 解锁是关键扣积分动作，必须交给后端事务处理。
export function unlockUserLead(id: number) {
  return clientHttp.post<UserLeadUnlockResult>(`${API_BASE}/${id}/unlock`)
}
