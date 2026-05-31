import type { RechargeOrder, RechargePackage } from './types'

export interface RechargeSummaryModel {
  eyebrow: string
  title: string
  description: string
}

export interface RechargeBalanceSummary {
  balance: number
}

export interface RechargePackageOption extends RechargePackage {
  amountText: string
  creditedPointsText: string
}

export const RECHARGE_COPY = {
  loadPackagesFailedText: '加载充值套餐失败',
  payFailedText: '发起支付失败',
  paymentParamsFailedText: '支付参数生成失败，请稍后重试',
  payButtonText: '立即充值并支付'
}

export function formatRechargeAmount(amountCents: number) {
  return `￥${(Number(amountCents || 0) / 100).toFixed(2)}`
}

export function buildRechargeSummary(balance: RechargeBalanceSummary | null): RechargeSummaryModel {
  return {
    eyebrow: 'Recharge Points',
    title: '补充积分，继续跟进商机',
    description: `当前余额 ${balance ? balance.balance : 0} 积分。充值成功后会自动回到结果页刷新状态。`
  }
}

export function toRechargePackageOptions(packages: RechargePackage[] = []): RechargePackageOption[] {
  return packages.map((item) => ({
    ...item,
    amountText: formatRechargeAmount(item.amountCents),
    creditedPointsText: `到账 ${item.creditedPoints} 积分`
  }))
}

export function pickDefaultRechargePackageCode(packages: RechargePackage[] = []) {
  return packages[0]?.code || ''
}

export function buildRechargeResultMeta(status: string, order: RechargeOrder | null) {
  if (status === 'success') {
    return {
      title: '支付成功',
      description: order ? `充值单已到账，获得 ${order.creditedPoints} 积分。` : '支付已完成，积分即将到账。'
    }
  }
  if (status === 'cancelled') {
    return {
      title: '支付已取消',
      description: '你可以重新选择套餐再次发起支付。'
    }
  }
  if (status === 'failed') {
    return {
      title: '支付失败',
      description: '请稍后重试，或检查网络后重新发起支付。'
    }
  }
  return {
    title: '支付处理中',
    description: '正在确认充值结果，请稍候刷新状态。'
  }
}

export function getRechargePackagesErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : RECHARGE_COPY.loadPackagesFailedText
}

export function getRechargePaymentErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : RECHARGE_COPY.payFailedText
}
