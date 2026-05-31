import { isFileChooseCancelledError } from '@forest/file/wechat-miniapp/upload'
import { chooseAndUploadCurrentUserAvatar } from './api'
import { getCurrentUserAvatarErrorMessage } from './view-model'
import type { CurrentUser } from './types'

declare const wx: {
  showToast(options: { title: string; icon: 'success' | 'none' }): void
}

export interface CurrentUserAvatarPageContext {
  data: {
    avatarUploading?: boolean
  }
  setData(patch: {
    user?: CurrentUser | null
    avatarUploading?: boolean
    errorMessage?: string
  }): void
}

export interface CurrentUserAvatarChangeHandlerOptions {
  chooseAndUploadCurrentUserAvatar?: () => Promise<CurrentUser>
  successToastTitle?: string
  failureToastTitle?: string
}

/**
 * 创建当前用户头像变更处理器。
 *
 * <p>小程序页面只负责把组件事件接到这个 handler。选择图片、上传文件、
 * 绑定头像、刷新页面 user、维护上传中状态和错误提示都收敛在 user 模块。</p>
 */
export function createCurrentUserAvatarChangeHandler(options: CurrentUserAvatarChangeHandlerOptions = {}) {
  return async function handleCurrentUserAvatarChange(this: CurrentUserAvatarPageContext) {
    if (this.data.avatarUploading) {
      return
    }
    this.setData({
      avatarUploading: true,
      errorMessage: ''
    })
    try {
      const uploadAvatar = options.chooseAndUploadCurrentUserAvatar || chooseAndUploadCurrentUserAvatar
      const user = await uploadAvatar()
      this.setData({ user })
      wx.showToast({ title: options.successToastTitle || '头像已更新', icon: 'success' })
    } catch (error) {
      if (isFileChooseCancelledError(error)) {
        this.setData({ errorMessage: '' })
        return
      }
      wx.showToast({
        title: options.failureToastTitle || getCurrentUserAvatarErrorMessage(error),
        icon: 'none'
      })
    } finally {
      this.setData({
        avatarUploading: false
      })
    }
  }
}
