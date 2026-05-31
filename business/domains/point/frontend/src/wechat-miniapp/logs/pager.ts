import type { PointLogPage } from './types'
import {
  getPointLogsErrorMessage,
  toPointLogDisplayItems,
  type PointLogDisplayItem
} from './view-model'

/**
 * 积分流水分页状态机。
 *
 * Page 负责下拉刷新、触底加载、返回行为；
 * point 域负责如何把页结果并入展示状态。
 */
export interface PointLogsPageState {
  logs: PointLogDisplayItem[]
  loading: boolean
  loadingMore: boolean
  errorMessage: string
  page: number
  size: number
  hasMore: boolean
}

export function createPointLogsPageState(): PointLogsPageState {
  return {
    logs: [],
    loading: false,
    loadingMore: false,
    errorMessage: '',
    page: 0,
    size: 20,
    hasMore: true
  }
}

export function startPointLogsLoading(
  state: PointLogsPageState,
  append: boolean
): Pick<PointLogsPageState, 'loading' | 'loadingMore' | 'errorMessage'> {
  return {
    loading: append ? state.loading : true,
    loadingMore: append,
    errorMessage: ''
  }
}

export function resolvePointLogsPage(
  state: PointLogsPageState,
  result: PointLogPage,
  requestedPage: number,
  append: boolean
): Pick<PointLogsPageState, 'logs' | 'page' | 'hasMore' | 'loading' | 'loadingMore'> {
  const logs = toPointLogDisplayItems(result.content || [])
  return {
    logs: append ? state.logs.concat(logs) : logs,
    page: result.number ?? requestedPage,
    hasMore: requestedPage + 1 < (result.totalPages || 0),
    loading: false,
    loadingMore: false
  }
}

export function failPointLogsPage(error: unknown): Pick<PointLogsPageState, 'errorMessage' | 'loading' | 'loadingMore'> {
  return {
    errorMessage: getPointLogsErrorMessage(error),
    loading: false,
    loadingMore: false
  }
}
