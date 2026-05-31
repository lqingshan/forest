import type { UserLeadListPage } from '../list/types'
import {
  getUnlockedLeadListErrorMessage,
  toUnlockedLeadCardModels,
  type UnlockedLeadCardModel
} from './view-model'

/**
 * 已解锁线索列表状态机。
 *
 * 它只关心分页与展示状态，不关心 onShow / onReachBottom 等平台时机。
 */
export interface UnlockedLeadListState {
  leads: UnlockedLeadCardModel[]
  loading: boolean
  loadingMore: boolean
  errorMessage: string
  page: number
  size: number
  hasMore: boolean
}

export function createUnlockedLeadListState(): UnlockedLeadListState {
  return {
    leads: [],
    loading: false,
    loadingMore: false,
    errorMessage: '',
    page: 0,
    size: 10,
    hasMore: true
  }
}

export function startUnlockedLeadListLoading(
  state: UnlockedLeadListState,
  append: boolean
): Pick<UnlockedLeadListState, 'loading' | 'loadingMore' | 'errorMessage'> {
  return {
    loading: append ? state.loading : true,
    loadingMore: append,
    errorMessage: ''
  }
}

export function resolveUnlockedLeadList(
  state: UnlockedLeadListState,
  result: UserLeadListPage,
  requestedPage: number,
  append: boolean
): Pick<UnlockedLeadListState, 'leads' | 'page' | 'hasMore' | 'loading' | 'loadingMore'> {
  const leads = toUnlockedLeadCardModels(result.content || [])
  return {
    leads: append ? state.leads.concat(leads) : leads,
    page: result.number ?? requestedPage,
    hasMore: requestedPage + 1 < (result.totalPages || 0),
    loading: false,
    loadingMore: false
  }
}

export function failUnlockedLeadList(error: unknown): Pick<UnlockedLeadListState, 'errorMessage' | 'loading' | 'loadingMore'> {
  return {
    errorMessage: getUnlockedLeadListErrorMessage(error),
    loading: false,
    loadingMore: false
  }
}
