import type { CurrentUser } from './types'

export interface CurrentUserSummaryModel {
  title: string
  description: string
  initial: string
}

export const CURRENT_USER_COPY = {
  defaultName: '微信用户',
  description: '管理账户资料与登录状态。',
  loadFailedText: '加载我的信息失败',
  avatarUpdateFailedText: '头像更新失败',
  logoutDialog: {
    title: '退出登录',
    content: '退出后需要重新进行微信登录，确定继续吗？'
  }
}

export function buildCurrentUserSummary(user: CurrentUser | null): CurrentUserSummaryModel {
  const title = user && user.name ? user.name : CURRENT_USER_COPY.defaultName
  return {
    title,
    description: CURRENT_USER_COPY.description,
    initial: title.slice(0, 1)
  }
}

export function getCurrentUserProfileErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : CURRENT_USER_COPY.loadFailedText
}

export function getCurrentUserAvatarErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : CURRENT_USER_COPY.avatarUpdateFailedText
}
