import { apiPaths, clientHttp } from '@forest/http-client'
import type { PointBalance } from './types'

// 当前积分余额用于“我的”、充值页和解锁前后的展示。
export function fetchPointBalance() {
  return clientHttp.get<PointBalance>(`${apiPaths.client}/point/balance`)
}
