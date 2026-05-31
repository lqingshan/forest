import type { RechargeOrder } from './types'
import { buildRechargeResultMeta } from './view-model'

export type RechargeResultStatus = 'success' | 'processing' | 'cancelled' | 'failed'

/**
 * 支付结果展示状态。
 *
 * Page 负责跳转行为；
 * recharge 域负责确认充值结果并映射最终展示文案。
 */
export interface RechargeResultPageState {
  status: RechargeResultStatus
  title: string
  description: string
  order: RechargeOrder | null
  rechargeOrderId: number
  loading: boolean
}

export function createRechargeResultPageState(): RechargeResultPageState {
  const meta = buildRechargeResultMeta('processing', null)
  return {
    status: 'processing',
    title: meta.title,
    description: meta.description,
    order: null,
    rechargeOrderId: 0,
    loading: false
  }
}

export function assignRechargeResultOrderId(rechargeOrderId: number): Pick<RechargeResultPageState, 'rechargeOrderId'> {
  return {
    rechargeOrderId
  }
}

export function startRechargeResultLoading(): Pick<RechargeResultPageState, 'loading'> {
  return {
    loading: true
  }
}

export function resolveRechargeResultState(
  status: RechargeResultStatus,
  order: RechargeOrder | null
): Pick<RechargeResultPageState, 'status' | 'title' | 'description' | 'order' | 'loading'> {
  const meta = buildRechargeResultMeta(status, order)
  return {
    status,
    title: meta.title,
    description: meta.description,
    order,
    loading: false
  }
}
