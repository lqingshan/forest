import type { UserLeadListItem } from './types'

export interface UserLeadListSummaryModel {
  eyebrow: string
  title: string
  description: string
}

export interface UserLeadListCardModel {
  id: number
  name: string
  category: string
  country: string
  unlocked: boolean
  phone: string
  website: string
  statusText: string
}

export const USER_LEAD_LIST_COPY = {
  emptyText: '暂无可展示的线索，试试更换关键词。',
  loadingMoreText: '加载更多线索中...',
  loadFailedText: '加载线索失败'
}

export function buildUserLeadListSummary(totalElements = 0): UserLeadListSummaryModel {
  return {
    eyebrow: 'Lead Pool',
    title: `${totalElements || 0} 条采购线索待筛选`,
    description: '优先浏览采购商基础资料，感兴趣后再进入详情页解锁联系方式。'
  }
}

export function toUserLeadListCardModel(item: UserLeadListItem): UserLeadListCardModel {
  return {
    id: item.id,
    name: item.name,
    category: item.category || '未分类',
    country: item.country || '未知地区',
    unlocked: item.unlocked,
    phone: item.phone || '',
    website: item.website || '',
    statusText: item.unlocked ? '已解锁' : '待解锁'
  }
}

export function toUserLeadListCardModels(items: UserLeadListItem[] = []) {
  return items.map(toUserLeadListCardModel)
}

export function getUserLeadListErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : USER_LEAD_LIST_COPY.loadFailedText
}
