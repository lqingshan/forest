import type { PointLogItem } from './types'

export interface PointLogsSummaryModel {
  eyebrow: string
  title: string
  description: string
}

export interface PointLogDisplayItem {
  id: number
  amountText: string
  sourceText: string
  balanceAfter: number
  createdTime: string
  direction: PointLogItem['direction']
}

export const POINT_LOGS_COPY = {
  emptyText: '暂无积分流水记录。',
  loadFailedText: '加载积分流水失败'
}

export function buildPointLogsSummary(): PointLogsSummaryModel {
  return {
    eyebrow: 'Point Ledger',
    title: '积分流水',
    description: '记录充值到账与线索解锁扣减，帮助你回看每一次积分变化。'
  }
}

export function formatPointSource(sourceType: string) {
  const mapping: Record<string, string> = {
    RECHARGE: '充值到账',
    UNLOCK: '线索解锁'
  }
  return mapping[sourceType] || sourceType || '未知来源'
}

export function formatPointDirection(direction: string, amount: number) {
  return `${direction === 'INCOME' ? '+' : '-'}${amount || 0}`
}

export function formatPointLogDateTime(value: string) {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

export function toPointLogDisplayItem(item: PointLogItem): PointLogDisplayItem {
  return {
    id: item.id,
    amountText: formatPointDirection(item.direction, item.amount),
    sourceText: formatPointSource(item.sourceType),
    balanceAfter: item.balanceAfter,
    createdTime: formatPointLogDateTime(item.createdTime),
    direction: item.direction
  }
}

export function toPointLogDisplayItems(items: PointLogItem[] = []) {
  return items.map(toPointLogDisplayItem)
}

export function getPointLogsErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : POINT_LOGS_COPY.loadFailedText
}
