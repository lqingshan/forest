import { completeUploadSession, createUploadSession, type FileApiOptions } from '../../shared/api'
import { fileNameFromPath, inferContentType, validateFileSize } from '../../shared/file-policy'
import type {
  FileCategory,
  FileInfo,
  FileUploadBatchResult,
  FileUploadItemResult,
  FileUploadProgress,
  UploadCredential
} from '../../shared/types'

type WechatRuntimeRecord = Record<string, any>

declare const wx: WechatRuntimeRecord

export class FileChooseCancelledError extends Error {
  constructor(message = '未选择文件') {
    super(message)
    this.name = 'FileChooseCancelledError'
  }
}

export function isFileChooseCancelledError(error: unknown): error is FileChooseCancelledError {
  return error instanceof FileChooseCancelledError
}

export interface WechatMiniappUploadFileInput {
  filePath: string
  name?: string
  sizeBytes: number
  fileCategory: FileCategory
  contentType?: string
  imageWidth?: number | null
  imageHeight?: number | null
}

export interface WechatMiniappUploadOptions extends FileApiOptions {
  onProgress?: (progress: FileUploadProgress) => void
}

export async function uploadWechatMiniappFile(
  input: WechatMiniappUploadFileInput,
  options: WechatMiniappUploadOptions = {}
): Promise<FileInfo> {
  const fileName = input.name || fileNameFromPath(input.filePath, defaultFileName(input.fileCategory))
  validateFileSize(input.fileCategory, input.sizeBytes)
  const contentType = inferContentType(fileName, input.contentType || '')
  const uploadSession = await createUploadSession({
    originalName: fileName,
    contentType,
    fileCategory: input.fileCategory,
    sizeBytes: input.sizeBytes,
    imageWidth: input.imageWidth ?? null,
    imageHeight: input.imageHeight ?? null
  }, options)

  await uploadFile(uploadSession.credential, input.filePath, options.onProgress)
  return completeUploadSession(uploadSession.uploadSessionNo, options)
}

export async function uploadWechatMiniappFiles(
  inputs: WechatMiniappUploadFileInput[],
  options: {
    concurrency?: number
    onFileProgress?: (index: number, progress: FileUploadProgress) => void
    onFileUploaded?: (index: number, file: FileInfo) => void
    onFileFailed?: (index: number, error: Error) => void
    onFileStatusChange?: (item: FileUploadItemResult) => void
  } & FileApiOptions = {}
): Promise<FileUploadBatchResult> {
  const concurrency = Math.max(1, options.concurrency || 3)
  const items: FileUploadItemResult[] = inputs.map((input, index) => ({
    index,
    name: input.name || fileNameFromPath(input.filePath, defaultFileName(input.fileCategory)),
    progress: 0,
    status: 'PENDING'
  }))
  let nextIndex = 0

  async function worker() {
    while (nextIndex < inputs.length) {
      const index = nextIndex
      nextIndex += 1
      try {
        updateItem(items, index, { status: 'UPLOADING' }, options.onFileStatusChange)
        const file = await uploadWechatMiniappFile(inputs[index], {
          httpClient: options.httpClient,
          scope: options.scope,
          apiBase: options.apiBase,
          onProgress: (progress) => {
            updateItem(items, index, { progress: progress.progress }, options.onFileStatusChange)
            options.onFileProgress?.(index, progress)
          }
        })
        updateItem(items, index, { file, progress: 100, status: 'SUCCESS' }, options.onFileStatusChange)
        options.onFileUploaded?.(index, file)
      } catch (error) {
        const normalizedError = normalizeError(error)
        updateItem(
          items,
          index,
          { errorMessage: normalizedError.message, status: 'FAILED' },
          options.onFileStatusChange
        )
        options.onFileFailed?.(index, normalizedError)
      }
    }
  }

  await Promise.all(Array.from({ length: Math.min(concurrency, inputs.length) }, worker))
  return toBatchResult(items)
}

export function chooseWechatMiniappFile(category: FileCategory): Promise<WechatMiniappUploadFileInput> {
  return chooseWechatMiniappFiles(category, 1).then((files) => files[0])
}

export function chooseWechatMiniappFiles(category: FileCategory, count = 9): Promise<WechatMiniappUploadFileInput[]> {
  if (category === 'IMAGE') {
    return chooseMedia('image', 'IMAGE', count)
  }
  if (category === 'VIDEO') {
    return chooseMedia('video', 'VIDEO', count)
  }
  return chooseMessageFiles(category, count)
}

function chooseMedia(mediaType: 'image' | 'video', fileCategory: FileCategory, count: number): Promise<WechatMiniappUploadFileInput[]> {
  return new Promise((resolve, reject) => {
    wx.chooseMedia({
      count,
      mediaType: [mediaType],
      sourceType: ['album', 'camera'],
      success(result: WechatRuntimeRecord) {
        const files = Array.isArray(result.tempFiles) ? result.tempFiles : []
        const normalizedFiles = files
          .map((file: WechatRuntimeRecord) => {
            const filePath = typeof file?.tempFilePath === 'string' ? file.tempFilePath : ''
            if (!filePath) {
              return null
            }
            return {
              filePath,
              name: fileNameFromPath(filePath, defaultFileName(fileCategory)),
              sizeBytes: Number(file.size || 0),
              fileCategory,
              imageWidth: fileCategory === 'IMAGE' ? Number(file.width || 0) || null : null,
              imageHeight: fileCategory === 'IMAGE' ? Number(file.height || 0) || null : null
            }
          })
          .filter(Boolean) as WechatMiniappUploadFileInput[]
        if (!normalizedFiles.length) {
          reject(new FileChooseCancelledError())
          return
        }
        resolve(normalizedFiles)
      },
      fail(error: WechatRuntimeRecord) {
        if (isWechatChooseCancel(error)) {
          reject(new FileChooseCancelledError())
          return
        }
        reject(new Error(error?.errMsg || '选择文件失败'))
      }
    })
  })
}

function chooseMessageFiles(category: Extract<FileCategory, 'DOCUMENT' | 'AUDIO'>, count: number): Promise<WechatMiniappUploadFileInput[]> {
  return new Promise((resolve, reject) => {
    wx.chooseMessageFile({
      count,
      type: 'file',
      success(result: WechatRuntimeRecord) {
        const files = Array.isArray(result.tempFiles) ? result.tempFiles : []
        const normalizedFiles = files
          .map((file: WechatRuntimeRecord) => {
            const filePath = typeof file?.path === 'string' ? file.path : ''
            if (!filePath) {
              return null
            }
            return {
              filePath,
              name: typeof file.name === 'string' ? file.name : fileNameFromPath(filePath, defaultFileName(category)),
              sizeBytes: Number(file.size || 0),
              fileCategory: category
            }
          })
          .filter(Boolean) as WechatMiniappUploadFileInput[]
        if (!normalizedFiles.length) {
          reject(new FileChooseCancelledError())
          return
        }
        resolve(normalizedFiles)
      },
      fail(error: WechatRuntimeRecord) {
        if (isWechatChooseCancel(error)) {
          reject(new FileChooseCancelledError())
          return
        }
        reject(new Error(error?.errMsg || '选择文件失败'))
      }
    })
  })
}

function isWechatChooseCancel(error: WechatRuntimeRecord) {
  const errMsg = typeof error?.errMsg === 'string' ? error.errMsg : ''
  const normalizedErrMsg = errMsg.toLowerCase()
  return normalizedErrMsg.includes('cancel') || errMsg.includes('取消')
}

function uploadFile(
  credential: UploadCredential,
  filePath: string,
  onProgress?: (progress: FileUploadProgress) => void
) {
  return new Promise<void>((resolve, reject) => {
    const uploadTask = wx.uploadFile({
      url: credential.uploadUrl,
      filePath,
      name: 'file',
      formData: credential.formFields || {},
      header: credential.headers || {},
      success(response: WechatRuntimeRecord) {
        const statusCode = Number(response.statusCode || 0)
        if (statusCode >= 200 && statusCode < 300) {
          resolve()
          return
        }
        reject(new Error(`文件上传失败：${statusCode}`))
      },
      fail(error: WechatRuntimeRecord) {
        reject(new Error(error?.errMsg || '文件上传失败，请检查网络'))
      }
    })

    if (uploadTask && typeof uploadTask.onProgressUpdate === 'function') {
      uploadTask.onProgressUpdate((progress: WechatRuntimeRecord) => {
        onProgress?.({
          progress: Number(progress.progress || 0),
          loaded: Number(progress.totalBytesSent || 0),
          total: Number(progress.totalBytesExpectedToSend || 0)
        })
      })
    }
  })
}

function updateItem(
  items: FileUploadItemResult[],
  index: number,
  patch: Partial<FileUploadItemResult>,
  onFileStatusChange?: (item: FileUploadItemResult) => void
) {
  items[index] = {
    ...items[index],
    ...patch
  }
  onFileStatusChange?.(items[index])
}

function toBatchResult(items: FileUploadItemResult[]): FileUploadBatchResult {
  const files = items.flatMap((item) => (item.file ? [item.file] : []))
  const failedItems = items.filter((item) => item.status === 'FAILED')
  return {
    items,
    files,
    failedItems,
    hasFailed: failedItems.length > 0
  }
}

function normalizeError(error: unknown): Error {
  return error instanceof Error ? error : new Error('文件上传失败')
}

function defaultFileName(category: FileCategory) {
  if (category === 'IMAGE') {
    return 'image.jpg'
  }
  if (category === 'VIDEO') {
    return 'video.mp4'
  }
  if (category === 'AUDIO') {
    return 'audio.mp3'
  }
  return 'document.pdf'
}
