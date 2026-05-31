import type { UserLeadListItem } from '../list/types'

export interface UnlockedLeadSummaryModel {
  eyebrow: string
  title: string
  description: string
}

export interface UnlockedLeadCardModel {
  id: number
  name: string
  category: string
  country: string
  phone: string
  website: string
}

export const UNLOCKED_LEAD_COPY = {
  emptyText: '还没有已解锁线索，去线索池挑选感兴趣的采购商吧。',
  loadingMoreText: '加载更多已解锁线索中...',
  loadFailedText: '加载已解锁线索失败'
}

export function buildUnlockedLeadSummary(): UnlockedLeadSummaryModel {
  return {
    eyebrow: 'Unlocked Leads',
    title: '我的已解锁线索',
    description: '这里沉淀了你已经解锁过的采购商联系方式，方便持续跟进。'
  }
}

export function toUnlockedLeadCardModel(item: UserLeadListItem): UnlockedLeadCardModel {
  return {
    id: item.id,
    name: item.name,
    category: item.category || '未分类',
    country: item.country || '未知地区',
    phone: item.phone || '',
    website: item.website || ''
  }
}

export function toUnlockedLeadCardModels(items: UserLeadListItem[] = []) {
  return items.map(toUnlockedLeadCardModel)
}

export function getUnlockedLeadListErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : UNLOCKED_LEAD_COPY.loadFailedText
}
