import type { UserLeadDetail } from './types'
import {
  getLeadDetailErrorMessage,
  getLeadUnlockErrorMessage,
  toLeadDetailCardModel,
  type LeadDetailCardModel
} from './view-model'

/**
 * 线索详情状态机。
 *
 * Page 仍然负责 wx.showModal、wx.setClipboardData、跳充值页等平台动作；
 * 这里负责详情页本身的加载/解锁状态与错误文案映射。
 */
export interface LeadDetailPageState {
  leadId: number
  lead: LeadDetailCardModel | null
  loading: boolean
  unlocking: boolean
  errorMessage: string
  unlockCost: number
}

export function createLeadDetailPageState(unlockCost: number): LeadDetailPageState {
  return {
    leadId: 0,
    lead: null,
    loading: false,
    unlocking: false,
    errorMessage: '',
    unlockCost
  }
}

export function assignLeadDetailId(leadId: number): Pick<LeadDetailPageState, 'leadId'> {
  return {
    leadId
  }
}

export function startLeadDetailLoading(): Pick<LeadDetailPageState, 'loading' | 'errorMessage'> {
  return {
    loading: true,
    errorMessage: ''
  }
}

export function resolveLeadDetail(lead: UserLeadDetail): Pick<LeadDetailPageState, 'lead' | 'loading'> {
  return {
    lead: toLeadDetailCardModel(lead),
    loading: false
  }
}

export function failLeadDetailLoading(error: unknown): Pick<LeadDetailPageState, 'errorMessage' | 'loading'> {
  return {
    errorMessage: getLeadDetailErrorMessage(error),
    loading: false
  }
}

export function startLeadUnlocking(): Pick<LeadDetailPageState, 'unlocking' | 'errorMessage'> {
  return {
    unlocking: true,
    errorMessage: ''
  }
}

export function failLeadUnlock(error: unknown): Pick<LeadDetailPageState, 'errorMessage' | 'unlocking'> {
  return {
    errorMessage: getLeadUnlockErrorMessage(error),
    unlocking: false
  }
}

export function finishLeadUnlocking(): Pick<LeadDetailPageState, 'unlocking'> {
  return {
    unlocking: false
  }
}
