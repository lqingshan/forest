import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  chooseWechatMiniappFiles,
  FileChooseCancelledError,
  isFileChooseCancelledError
} from './api'

describe('wechat miniapp upload file chooser', () => {
  beforeEach(() => {
    vi.stubGlobal('wx', {
      chooseMedia: vi.fn(),
      chooseMessageFile: vi.fn()
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it('treats empty media files as cancelled selection', async () => {
    const wx = (globalThis as MiniappRecord).wx
    wx.chooseMedia.mockImplementation((options: MiniappRecord) => {
      options.success({ tempFiles: [] })
    })

    await expect(chooseWechatMiniappFiles('IMAGE')).rejects.toBeInstanceOf(FileChooseCancelledError)
  })

  it('treats chooseMedia cancel failure as cancelled selection', async () => {
    const wx = (globalThis as MiniappRecord).wx
    wx.chooseMedia.mockImplementation((options: MiniappRecord) => {
      options.fail({ errMsg: 'chooseMedia:fail cancel' })
    })

    try {
      await chooseWechatMiniappFiles('IMAGE')
      throw new Error('expected chooseWechatMiniappFiles to reject')
    } catch (error) {
      expect(isFileChooseCancelledError(error)).toBe(true)
    }
  })

  it('treats chooseMessageFile cancel failure as cancelled selection', async () => {
    const wx = (globalThis as MiniappRecord).wx
    wx.chooseMessageFile.mockImplementation((options: MiniappRecord) => {
      options.fail({ errMsg: 'chooseMessageFile:fail 用户取消' })
    })

    await expect(chooseWechatMiniappFiles('DOCUMENT')).rejects.toBeInstanceOf(FileChooseCancelledError)
  })

  it('keeps non-cancel chooser failures as regular errors', async () => {
    const wx = (globalThis as MiniappRecord).wx
    wx.chooseMedia.mockImplementation((options: MiniappRecord) => {
      options.fail({ errMsg: 'chooseMedia:fail auth deny' })
    })

    try {
      await chooseWechatMiniappFiles('IMAGE')
      throw new Error('expected chooseWechatMiniappFiles to reject')
    } catch (error) {
      expect(isFileChooseCancelledError(error)).toBe(false)
      expect(error).toBeInstanceOf(Error)
      expect((error as Error).message).toBe('chooseMedia:fail auth deny')
    }
  })
})
