export type FileCategory = 'IMAGE' | 'DOCUMENT' | 'VIDEO' | 'AUDIO'

export type FileStatus = 'UPLOADING' | 'AVAILABLE' | 'FAILED' | 'DELETED'

export interface UploadCredential {
  uploadUrl: string
  method: string
  formFields: Record<string, string>
  headers: Record<string, string>
  expiresAt: string
}

export interface FileInfo {
  fileNo: string
  businessAppCode: string
  uploadedClientAppCode: string
  originalName: string
  contentType: string
  fileCategory: FileCategory
  sizeBytes: number
  etag: string | null
  status: FileStatus
  createdTime: string
}

export interface UploadSessionResult {
  uploadSessionNo: string
  fileNo: string
  bucket: string
  objectKey: string
  credential: UploadCredential
  file: FileInfo
}

export interface CreateUploadSessionPayload {
  originalName: string
  contentType: string
  fileCategory: FileCategory
  sizeBytes: number
  sha256?: string | null
  imageWidth?: number | null
  imageHeight?: number | null
}

export interface FileAccessUrlResult {
  fileNo: string
  url: string
  expiresAt: string
}

export type DownloadUrlResult = FileAccessUrlResult

export interface FileUploadProgress {
  progress: number
  loaded: number
  total: number
}

export type FileUploadItemStatus = 'PENDING' | 'UPLOADING' | 'SUCCESS' | 'FAILED'

export interface FileUploadItemResult {
  index: number
  name: string
  progress: number
  status: FileUploadItemStatus
  file?: FileInfo
  errorMessage?: string
}

export interface FileUploadBatchResult {
  items: FileUploadItemResult[]
  files: FileInfo[]
  failedItems: FileUploadItemResult[]
  hasFailed: boolean
}

export interface FileViewerSource {
  file: FileInfo
  previewUrl?: string
  downloadUrl?: string
}

export type FileViewerKind = 'IMAGE' | 'VIDEO' | 'AUDIO' | 'DOCUMENT'

export interface SingleFileViewerModel {
  kind: FileViewerKind
  title: string
  contentType: string
  previewUrl: string
  downloadUrl: string
  canPreview: boolean
  canDownload: boolean
  pdf: boolean
}
