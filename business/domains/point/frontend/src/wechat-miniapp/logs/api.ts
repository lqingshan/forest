import { apiPaths, clientHttp } from '@forest/http-client'
import type { PointLogPage } from './types'

// 积分流水用于核对充值入账和解锁扣减，后端按时间倒序返回。
export function fetchPointLogs(page = 0, size = 20) {
  return clientHttp.get<PointLogPage>(`${apiPaths.client}/point/logs/page`, {
    params: { page, size }
  })
}
