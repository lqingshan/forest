import { buildSingleFileViewerModel } from '../../../shared/viewer'
import type { FileInfo, SingleFileViewerModel } from '../../../shared/types'

type WechatRuntimeRecord = Record<string, any>

declare const wx: WechatRuntimeRecord
declare function Component(options: WechatRuntimeRecord & ThisType<WechatRuntimeRecord>): void

Component({
  externalClasses: [
    'viewer-class',
    'media-class',
    'body-class',
    'button-class'
  ],

  properties: {
    file: {
      type: Object,
      value: null
    },
    previewUrl: {
      type: String,
      value: ''
    },
    downloadUrl: {
      type: String,
      value: ''
    }
  },

  data: {
    model: null as SingleFileViewerModel | null,
    audioPlaying: false
  },

  observers: {
    'file,previewUrl,downloadUrl': function updateModel(file: FileInfo | null, previewUrl: string, downloadUrl: string) {
      this.setData({
        model: file ? buildSingleFileViewerModel({ file, previewUrl, downloadUrl }) : null
      })
    }
  },

  lifetimes: {
    detached() {
      this.destroyAudio()
    }
  },

  methods: {
    handlePreviewImage() {
      const model = this.data.model as SingleFileViewerModel | null
      if (!model?.previewUrl) {
        return
      }
      wx.previewImage({
        current: model.previewUrl,
        urls: [model.previewUrl]
      })
    },

    handleOpenDocument() {
      const model = this.data.model as SingleFileViewerModel | null
      const url = model?.previewUrl || model?.downloadUrl
      if (!url) {
        return
      }
      wx.downloadFile({
        url,
        success(result: WechatRuntimeRecord) {
          const filePath = String(result.tempFilePath || '')
          if (!filePath) {
            wx.showToast({ title: '文件打开失败', icon: 'none' })
            return
          }
          wx.openDocument({
            filePath,
            showMenu: true,
            fail() {
              wx.showToast({ title: '文件打开失败', icon: 'none' })
            }
          })
        },
        fail() {
          wx.showToast({ title: '文件下载失败', icon: 'none' })
        }
      })
    },

    handleDownload() {
      this.handleOpenDocument()
    },

    handleToggleAudio() {
      const model = this.data.model as SingleFileViewerModel | null
      if (!model?.previewUrl) {
        return
      }
      const component = this as WechatRuntimeRecord
      if (!component.audioContext) {
        component.audioContext = wx.createInnerAudioContext()
        component.audioContext.src = model.previewUrl
        component.audioContext.onEnded(() => this.setData({ audioPlaying: false }))
        component.audioContext.onStop(() => this.setData({ audioPlaying: false }))
        component.audioContext.onPause(() => this.setData({ audioPlaying: false }))
        component.audioContext.onError(() => {
          this.setData({ audioPlaying: false })
          wx.showToast({ title: '音频播放失败', icon: 'none' })
        })
      }

      if (this.data.audioPlaying) {
        component.audioContext.pause()
        this.setData({ audioPlaying: false })
        return
      }
      component.audioContext.play()
      this.setData({ audioPlaying: true })
    },

    destroyAudio() {
      const component = this as WechatRuntimeRecord
      if (!component.audioContext) {
        return
      }
      component.audioContext.destroy()
      component.audioContext = null
    }
  }
} as WechatRuntimeRecord)
