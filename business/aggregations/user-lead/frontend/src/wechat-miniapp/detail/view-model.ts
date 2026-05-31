import type { UserLeadDetail } from './types'

export interface LeadDetailCardModel extends UserLeadDetail {
  categoryText: string
  countryText: string
  introText: string
}

export const LEAD_DETAIL_COPY = {
  loadFailedText: '加载线索详情失败',
  unlockFailedText: '解锁失败',
  unlockSuccessText: '解锁成功',
  insufficientPointsDialog: {
    title: '积分不足',
    content: '当前积分不足以解锁该线索，是否立即前往充值？',
    confirmText: '去充值'
  }
}

export function toLeadDetailCardModel(lead: UserLeadDetail): LeadDetailCardModel {
  return {
    ...lead,
    categoryText: lead.category || '未分类',
    countryText: lead.country || '未知地区',
    introText: lead.intro || '暂无采购信息补充说明。'
  }
}

export function getLeadDetailErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : LEAD_DETAIL_COPY.loadFailedText
}

export function getLeadUnlockErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : LEAD_DETAIL_COPY.unlockFailedText
}

export function isInsufficientPointsMessage(message: string) {
  return message.includes('积分不足')
}
