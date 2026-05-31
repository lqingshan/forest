import type { FileViewerSource, SingleFileViewerModel } from './types'

export function buildSingleFileViewerModel(source: FileViewerSource): SingleFileViewerModel {
  const file = source.file
  const contentType = file.contentType || ''
  const previewUrl = source.previewUrl || ''
  const downloadUrl = source.downloadUrl || ''
  const pdf = contentType.toLowerCase() === 'application/pdf'

  return {
    kind: file.fileCategory,
    title: file.originalName || file.fileNo,
    contentType,
    previewUrl,
    downloadUrl,
    canPreview: Boolean(previewUrl) && (file.fileCategory !== 'DOCUMENT' || pdf),
    canDownload: Boolean(downloadUrl),
    pdf
  }
}
