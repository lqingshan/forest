import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { FileChooseCancelledError } from '@forest/file/wechat-miniapp/upload'
import {
  createCurrentUserAvatarChangeHandler,
  type CurrentUserAvatarPageContext
} from './avatar-flow'
import type { CurrentUser } from './types'

describe('current user avatar flow', () => {
  beforeEach(() => {
    vi.stubGlobal('wx', {
      showToast: vi.fn()
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it('silently ignores cancelled avatar selection', async () => {
    const context = createPageContext()
    context.data.errorMessage = '旧错误'
    const handler = createCurrentUserAvatarChangeHandler({
      chooseAndUploadCurrentUserAvatar: vi.fn().mockRejectedValue(new FileChooseCancelledError())
    })

    await handler.call(context)

    expect(context.data.errorMessage).toBe('')
    expect(context.data.avatarUploading).toBe(false)
    expect((globalThis as MiniappRecord).wx.showToast).not.toHaveBeenCalled()
  })

  it('shows toast without persistent page error when avatar upload fails', async () => {
    const context = createPageContext()
    const handler = createCurrentUserAvatarChangeHandler({
      chooseAndUploadCurrentUserAvatar: vi.fn().mockRejectedValue(new Error('头像文件不可用'))
    })

    await handler.call(context)

    expect(context.data.errorMessage).toBe('')
    expect(context.data.avatarUploading).toBe(false)
    expect((globalThis as MiniappRecord).wx.showToast).toHaveBeenCalledWith({
      title: '头像文件不可用',
      icon: 'none'
    })
  })

  it('updates user and shows success toast after avatar upload succeeds', async () => {
    const context = createPageContext()
    const user = createUser()
    const handler = createCurrentUserAvatarChangeHandler({
      chooseAndUploadCurrentUserAvatar: vi.fn().mockResolvedValue(user)
    })

    await handler.call(context)

    expect(context.data.user).toBe(user)
    expect(context.data.avatarUploading).toBe(false)
    expect((globalThis as MiniappRecord).wx.showToast).toHaveBeenCalledWith({
      title: '头像已更新',
      icon: 'success'
    })
  })
})

function createPageContext(): CurrentUserAvatarPageContext & {
  data: {
    user: CurrentUser | null
    avatarUploading: boolean
    errorMessage: string
  }
} {
  return {
    data: {
      user: null,
      avatarUploading: false,
      errorMessage: ''
    },
    setData(patch) {
      this.data = {
        ...this.data,
        ...patch
      }
    }
  }
}

function createUser(): CurrentUser {
  return {
    id: 1,
    name: 'Alice',
    avatar: 'FILE1',
    avatarUrl: 'https://forest.example/avatar.png',
    phone: '13800138000',
    email: null,
    status: 'ACTIVE'
  }
}
