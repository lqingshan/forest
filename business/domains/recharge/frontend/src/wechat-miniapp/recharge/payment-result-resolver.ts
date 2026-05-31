import { fetchRechargeOrder } from './api'
import type { RechargeResultStatus } from './result-state'
import type { RechargeOrder } from './types'

const DEFAULT_MAX_ATTEMPTS = 5
const DEFAULT_POLL_INTERVAL_MS = 900

export interface ResolveRechargePaymentResultOptions {
  rechargeOrderId: number
  initialStatus: RechargeResultStatus
  maxAttempts?: number
  pollIntervalMs?: number
  wait?: (milliseconds: number) => Promise<void>
}

export interface RechargePaymentResultResolution {
  status: RechargeResultStatus
  order: RechargeOrder | null
}

function waitFor(milliseconds: number) {
  return new Promise<void>((resolve) => {
    setTimeout(resolve, milliseconds)
  })
}

export async function resolveRechargePaymentResult({
  rechargeOrderId,
  initialStatus,
  maxAttempts = DEFAULT_MAX_ATTEMPTS,
  pollIntervalMs = DEFAULT_POLL_INTERVAL_MS,
  wait = waitFor
}: ResolveRechargePaymentResultOptions): Promise<RechargePaymentResultResolution> {
  let order: RechargeOrder | null = null
  let status: RechargeResultStatus = initialStatus

  if (rechargeOrderId) {
    // wx.requestPayment success 不是最终到账凭证，必须以后端充值单 PAID 为准。
    order = await pollRechargeOrderUntilPaid(rechargeOrderId, {
      maxAttempts,
      pollIntervalMs,
      wait
    })
    if (order && order.status === 'PAID') {
      status = 'success'
    } else if (status === 'success') {
      status = 'processing'
    }
  }

  return {
    status,
    order
  }
}

export async function pollRechargeOrderUntilPaid(
  rechargeOrderId: number,
  {
    maxAttempts = DEFAULT_MAX_ATTEMPTS,
    pollIntervalMs = DEFAULT_POLL_INTERVAL_MS,
    wait = waitFor
  }: Pick<ResolveRechargePaymentResultOptions, 'maxAttempts' | 'pollIntervalMs' | 'wait'> = {}
) {
  let lastOrder: RechargeOrder | null = null

  // 给微信异步通知留一点传播时间；超时后保留处理中，用户可手动刷新。
  for (let attempt = 0; attempt < maxAttempts; attempt += 1) {
    lastOrder = await fetchRechargeOrder(rechargeOrderId)
    if (lastOrder.status === 'PAID') {
      return lastOrder
    }
    await wait(pollIntervalMs)
  }

  return lastOrder
}
