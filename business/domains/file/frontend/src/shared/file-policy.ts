import type { FileCategory } from './types'

export const FILE_SIZE_LIMITS: Record<FileCategory, number> = {
  IMAGE: 10 * 1024 * 1024,
  DOCUMENT: 50 * 1024 * 1024,
  VIDEO: 50 * 1024 * 1024,
  AUDIO: 50 * 1024 * 1024
}

const CONTENT_TYPES_BY_EXTENSION: Record<string, string> = {
  jpg: 'image/jpeg',
  jpeg: 'image/jpeg',
  png: 'image/png',
  webp: 'image/webp',
  gif: 'image/gif',
  pdf: 'application/pdf',
  doc: 'application/msword',
  docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  xls: 'application/vnd.ms-excel',
  xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ppt: 'application/vnd.ms-powerpoint',
  pptx: 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
  txt: 'text/plain',
  mp4: 'video/mp4',
  mov: 'video/quicktime',
  avi: 'video/x-msvideo',
  mp3: 'audio/mpeg',
  m4a: 'audio/mp4',
  aac: 'audio/aac',
  wav: 'audio/wav',
  ogg: 'audio/ogg',
  flac: 'audio/flac'
}

export function validateFileSize(category: FileCategory, sizeBytes: number) {
  const limit = FILE_SIZE_LIMITS[category]
  if (!sizeBytes || sizeBytes <= 0) {
    throw new Error('文件大小必须大于 0')
  }
  if (sizeBytes > limit) {
    throw new Error('文件大小超过限制')
  }
}

export function inferContentType(fileName: string, fallback = '') {
  if (fallback) {
    return fallback
  }
  const extension = fileName.split('.').pop()?.toLowerCase() || ''
  return CONTENT_TYPES_BY_EXTENSION[extension] || 'application/octet-stream'
}

export function fileNameFromPath(path: string, fallback: string) {
  const normalized = path.replace(/\\/g, '/')
  const name = normalized.split('/').filter(Boolean).pop()
  return name || fallback
}
