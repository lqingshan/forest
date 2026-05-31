import { apiPaths, clientHttp, type HttpClient } from '@forest/http-client'
import type { CreateUploadSessionPayload, FileAccessUrlResult, FileInfo, UploadSessionResult } from './types'

export type FileApiScope = 'client' | 'admin' | 'platform'

export interface FileApiOptions {
  httpClient?: HttpClient
  scope?: FileApiScope
  apiBase?: string
}

export function createUploadSession(payload: CreateUploadSessionPayload, options?: FileApiOptions) {
  return resolveHttpClient(options).post<UploadSessionResult>(`${resolveApiBase(options)}/upload-session`, payload)
}

export function completeUploadSession(uploadSessionNo: string, options?: FileApiOptions) {
  return resolveHttpClient(options).post<FileInfo>(
    `${resolveApiBase(options)}/upload-session/${encodeURIComponent(uploadSessionNo)}/complete`
  )
}

export function abortUploadSession(uploadSessionNo: string, options?: FileApiOptions) {
  return resolveHttpClient(options).post<FileInfo>(
    `${resolveApiBase(options)}/upload-session/${encodeURIComponent(uploadSessionNo)}/abort`
  )
}

export function fetchFile(fileNo: string, options?: FileApiOptions) {
  return resolveHttpClient(options).get<FileInfo>(`${resolveApiBase(options)}/${encodeURIComponent(fileNo)}`)
}

export function createDownloadUrls(fileNos: string[], options?: FileApiOptions) {
  return resolveHttpClient(options).post<FileAccessUrlResult[]>(`${resolveApiBase(options)}/download-url`, { fileNos })
}

export function createPreviewUrls(fileNos: string[], options?: FileApiOptions) {
  return resolveHttpClient(options).post<FileAccessUrlResult[]>(`${resolveApiBase(options)}/preview-url`, { fileNos })
}

export function deleteFile(fileNo: string, options?: FileApiOptions) {
  return resolveHttpClient(options).delete<void>(`${resolveApiBase(options)}/${encodeURIComponent(fileNo)}`)
}

function resolveHttpClient(options?: FileApiOptions) {
  return options?.httpClient ?? clientHttp
}

function resolveApiBase(options?: FileApiOptions) {
  if (options?.apiBase) {
    return options.apiBase
  }
  const scope = options?.scope ?? 'client'
  return `${apiPaths[scope]}/file`
}
