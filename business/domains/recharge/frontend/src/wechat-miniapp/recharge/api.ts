import { apiPaths, clientHttp } from '@forest/http-client'
import type { RechargeOrder, RechargePackage } from './types'

const API_BASE = `${apiPaths.client}/recharge`

// 充值域只负责套餐和充值主单；支付参数由 payment 包创建。
export function fetchRechargePackages() {
  return clientHttp.get<RechargePackage[]>(`${API_BASE}/packages`)
}

export function createRechargeOrder(packageCode: string) {
  return clientHttp.post<RechargeOrder>(`${API_BASE}/orders`, { packageCode })
}

export function fetchRechargeOrder(id: number) {
  return clientHttp.get<RechargeOrder>(`${API_BASE}/orders/${id}`)
}
