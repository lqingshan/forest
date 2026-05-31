import type { UserLeadListPage, UserLeadListQuery } from './types'
import {
  getUserLeadListErrorMessage,
  toUserLeadListCardModels,
  type UserLeadListCardModel
} from './view-model'

/**
 * 线索列表分页状态机。
 *
 * Page 仍然负责生命周期、路由和最终 setData；
 * 这个文件只负责“下一份业务状态应该长什么样”。
 */
export interface UserLeadListPageState {
  leads: UserLeadListCardModel[]
  loading: boolean
  loadingMore: boolean
  errorMessage: string
  keyword: string
  page: number
  size: number
  totalElements: number
  hasMore: boolean
}

export function createUserLeadListPageState(): UserLeadListPageState {
  return {
    leads: [],
    loading: false,
    loadingMore: false,
    errorMessage: '',
    keyword: '',
    page: 0,
    size: 10,
    totalElements: 0,
    hasMore: true
  }
}

export function buildUserLeadListQuery(state: Pick<UserLeadListPageState, 'page' | 'size' | 'keyword'>): UserLeadListQuery {
  return {
    page: state.page,
    size: state.size,
    keyword: state.keyword.trim()
  }
}

export function updateUserLeadListKeyword(
  state: UserLeadListPageState,
  keyword: string
): Pick<UserLeadListPageState, 'keyword'> {
  return {
    keyword
  }
}

export function startUserLeadListLoading(
  state: UserLeadListPageState,
  append: boolean
): Pick<UserLeadListPageState, 'loading' | 'loadingMore' | 'errorMessage'> {
  return {
    loading: append ? state.loading : true,
    loadingMore: append,
    errorMessage: ''
  }
}

export function resolveUserLeadListPage(
  state: UserLeadListPageState,
  result: UserLeadListPage,
  requestedPage: number,
  append: boolean
): Pick<UserLeadListPageState, 'leads' | 'page' | 'totalElements' | 'hasMore' | 'loading' | 'loadingMore'> {
  const leads = toUserLeadListCardModels(result.content || [])
  return {
    leads: append ? state.leads.concat(leads) : leads,
    page: result.number ?? requestedPage,
    totalElements: result.totalElements || 0,
    hasMore: requestedPage + 1 < (result.totalPages || 0),
    loading: false,
    loadingMore: false
  }
}

export function failUserLeadListPage(error: unknown): Pick<UserLeadListPageState, 'errorMessage' | 'loading' | 'loadingMore'> {
  return {
    errorMessage: getUserLeadListErrorMessage(error),
    loading: false,
    loadingMore: false
  }
}
