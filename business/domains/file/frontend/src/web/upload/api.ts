import type { HttpClient } from '@forest/http-client'
import { completeUploadSession, createUploadSession, type FileApiScope } from '../../shared/api'
import { inferContentType, validateFileSize } from '../../shared/file-policy'
import type {
  FileCategory,
  FileInfo,
  FileUploadBatchResult,
  FileUploadItemResult,
  FileUploadProgress,
  UploadCredential
} from '../../shared/types'

export interface WebFileUploadOptions {
  file: File
  fileCategory: FileCategory
  imageWidth?: number | null
  imageHeight?: number | null
  httpClient?: HttpClient
  scope?: FileApiScope
  apiBase?: string
  onProgress?: (progress: FileUploadProgress) => void
}

export interface WebMultiFileUploadOptions {
  files: File[]
  fileCategory: FileCategory
  concurrency?: number
  httpClient?: HttpClient
  scope?: FileApiScope
  apiBase?: string
  onFileProgress?: (index: number, progress: FileUploadProgress) => void
  onFileUploaded?: (index: number, file: FileInfo) => void
  onFileFailed?: (index: number, error: Error) => void
  onFileStatusChange?: (item: FileUploadItemResult) => void
}

export async function uploadWebFile(options: WebFileUploadOptions): Promise<FileInfo> {
  validateFileSize(options.fileCategory, options.file.size)
  const contentType = inferContentType(options.file.name, options.file.type)
  const uploadSession = await createUploadSession({
    originalName: options.file.name,
    contentType,
    fileCategory: options.fileCategory,
    sizeBytes: options.file.size,
    imageWidth: options.imageWidth ?? null,
    imageHeight: options.imageHeight ?? null
  }, {
    httpClient: options.httpClient,
    scope: options.scope,
    apiBase: options.apiBase
  })

  await uploadFileToObjectStorage(uploadSession.credential, options.file, options.onProgress)
  return completeUploadSession(uploadSession.uploadSessionNo, {
    httpClient: options.httpClient,
    scope: options.scope,
    apiBase: options.apiBase
  })
}

export async function uploadWebFiles(options: WebMultiFileUploadOptions): Promise<FileUploadBatchResult> {
  const concurrency = Math.max(1, options.concurrency || 3)
  const items: FileUploadItemResult[] = options.files.map((file, index) => ({
    index,
    name: file.name,
    progress: 0,
    status: 'PENDING'
  }))
  let nextIndex = 0

  async function worker() {
    while (nextIndex < options.files.length) {
      const index = nextIndex
      nextIndex += 1
      try {
        updateItem(items, index, { status: 'UPLOADING' }, options.onFileStatusChange)
        const file = await uploadWebFile({
          file: options.files[index],
          fileCategory: options.fileCategory,
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

  await Promise.all(Array.from({ length: Math.min(concurrency, options.files.length) }, worker))
  return toBatchResult(items)
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

function uploadFileToObjectStorage(
  credential: UploadCredential,
  file: File,
  onProgress?: (progress: FileUploadProgress) => void
) {
  return new Promise<void>((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    const formData = new FormData()
    for (const [key, value] of Object.entries(credential.formFields || {})) {
      formData.append(key, value)
    }
    formData.append('file', file)

    xhr.open(credential.method || 'POST', credential.uploadUrl)
    for (const [key, value] of Object.entries(credential.headers || {})) {
      xhr.setRequestHeader(key, value)
    }

    xhr.upload.onprogress = (event) => {
      if (!event.lengthComputable || !onProgress) {
        return
      }
      onProgress({
        progress: Math.round((event.loaded / event.total) * 100),
        loaded: event.loaded,
        total: event.total
      })
    }

    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        resolve()
        return
      }
      reject(new Error(`文件上传失败：${xhr.status}`))
    }
    xhr.onerror = () => reject(new Error('文件上传失败，请检查网络'))
    xhr.onabort = () => reject(new Error('文件上传已取消'))
    xhr.send(formData)
  })
}
