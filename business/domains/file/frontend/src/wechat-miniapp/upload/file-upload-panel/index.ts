import { chooseWechatMiniappFiles, uploadWechatMiniappFile, uploadWechatMiniappFiles } from '../api'
import { FILE_UPLOAD_COPY, getFileUploadErrorMessage } from '../view-model'
import type { FileCategory, FileInfo, FileUploadItemStatus, FileUploadProgress } from '../../../shared/types'
import type { WechatMiniappUploadFileInput } from '../api'

interface UploadPanelFileItem {
  name: string
  progress: number
  status: FileUploadItemStatus
  input: WechatMiniappUploadFileInput
  file?: FileInfo
  errorMessage?: string
}

function normalizeCategory(value: string): FileCategory {
  if (value === 'DOCUMENT' || value === 'VIDEO' || value === 'AUDIO') {
    return value
  }
  return 'IMAGE'
}

Component({
  externalClasses: [
    'panel-class',
    'heading-class',
    'button-class',
    'progress-class',
    'file-list-class',
    'file-item-class',
    'retry-button-class',
    'error-class'
  ],

  properties: {
    category: {
      type: String,
      value: 'IMAGE'
    },
    disabled: {
      type: Boolean,
      value: false
    },
    maxCount: {
      type: Number,
      value: 9
    }
  },

  data: {
    copy: FILE_UPLOAD_COPY.IMAGE,
    uploading: false,
    progress: 0,
    files: [],
    errorMessage: ''
  },

  observers: {
    category(value: string) {
      const category = normalizeCategory(value)
      this.setData({
        copy: FILE_UPLOAD_COPY[category]
      })
    }
  },

  methods: {
    async handleChooseFile() {
      if (this.properties.disabled || this.data.uploading) {
        return
      }

      const category = normalizeCategory(this.properties.category)
      this.setData({
        uploading: true,
        progress: 0,
        files: [],
        errorMessage: '',
        copy: FILE_UPLOAD_COPY[category]
      })

      try {
        const files = await chooseWechatMiniappFiles(category, Math.max(1, Number(this.properties.maxCount || 1)))
        this.setData({
          files: files.map((file) => ({
            name: file.name || file.filePath,
            progress: 0,
            status: 'PENDING',
            input: file
          }))
        })
        const results = await uploadWechatMiniappFiles(files, {
          concurrency: Math.min(3, Math.max(1, Number(this.properties.maxCount || 1))),
          onFileStatusChange: (item) => {
            this.updateFileItem(item.index, {
              file: item.file,
              progress: item.progress,
              status: item.status,
              errorMessage: item.errorMessage || ''
            })
          },
          onFileProgress: (index, progress) => {
            this.updateFileItem(index, { progress: progress.progress })
            this.triggerEvent('progress', progress)
          }
        })
        this.setData({
          progress: this.calculateTotalProgress()
        })
        this.triggerEvent('uploaded', {
          files: results.files,
          failedItems: results.failedItems,
          items: results.items
        })
        if (results.hasFailed) {
          this.setData({
            errorMessage: `${results.failedItems.length} 个文件上传失败，可单独重试`
          })
        }
      } catch (error) {
        const message = getFileUploadErrorMessage(error)
        this.setData({
          errorMessage: message
        })
        this.triggerEvent('uploaderror', {
          message
        })
      } finally {
        this.setData({
          uploading: false
        })
      }
    },

    async handleRetryFile(event: MiniappRecord) {
      if (this.data.uploading) {
        return
      }
      const index = Number(event?.currentTarget?.dataset?.index)
      const item = this.data.files[index] as UploadPanelFileItem | undefined
      if (!item || !item.input) {
        return
      }

      this.updateFileItem(index, {
        progress: 0,
        status: 'UPLOADING',
        errorMessage: ''
      })
      this.setData({
        uploading: true,
        errorMessage: ''
      })

      try {
        const file = await uploadWechatMiniappFile(item.input, {
          onProgress: (progress: FileUploadProgress) => {
            this.updateFileItem(index, {
              progress: progress.progress
            })
            this.triggerEvent('progress', progress)
          }
        })
        this.updateFileItem(index, {
          file,
          progress: 100,
          status: 'SUCCESS',
          errorMessage: ''
        })
        this.triggerEvent('uploaded', {
          files: this.collectSuccessfulFiles(),
          failedItems: this.collectFailedItems()
        })
      } catch (error) {
        const message = getFileUploadErrorMessage(error)
        this.updateFileItem(index, {
          errorMessage: message,
          status: 'FAILED'
        })
        this.setData({
          errorMessage: message
        })
        this.triggerEvent('uploaderror', {
          message
        })
      } finally {
        this.setData({
          uploading: false
        })
      }
    },

    updateFileItem(index: number, patch: Partial<UploadPanelFileItem>) {
      const nextFiles = [...this.data.files] as UploadPanelFileItem[]
      nextFiles[index] = {
        ...nextFiles[index],
        ...patch
      }
      this.setData({
        files: nextFiles,
        progress: calculateTotalProgress(nextFiles)
      })
    },

    calculateTotalProgress() {
      return calculateTotalProgress(this.data.files as UploadPanelFileItem[])
    },

    collectSuccessfulFiles() {
      return (this.data.files as UploadPanelFileItem[])
        .filter((item) => item.status === 'SUCCESS' && item.file)
        .map((item) => item.file as FileInfo)
    },

    collectFailedItems() {
      return (this.data.files as UploadPanelFileItem[])
        .map((item, index) => ({
          index,
          name: item.name,
          progress: item.progress,
          status: item.status,
          errorMessage: item.errorMessage
        }))
        .filter((item) => item.status === 'FAILED')
    }
  }
})

function calculateTotalProgress(files: UploadPanelFileItem[]) {
  if (!files.length) {
    return 0
  }
  return Math.round(files.reduce((sum, item) => sum + Number(item.progress || 0), 0) / files.length)
}
