import { describe, expect, it } from 'vitest'
import { buildSingleFileViewerModel } from './viewer'
import type { FileInfo } from './types'

const baseFile: FileInfo = {
  fileNo: 'FILE1',
  businessAppCode: 'cxc-commerce',
  uploadedClientAppCode: 'cxc-commerce-buyer-wechat-miniapp',
  originalName: 'contract.pdf',
  contentType: 'application/pdf',
  fileCategory: 'DOCUMENT',
  sizeBytes: 1024,
  etag: 'etag-1',
  status: 'AVAILABLE',
  createdTime: '2026-05-11T10:00:00'
}

describe('single file viewer model', () => {
  it('allows pdf preview and download', () => {
    const model = buildSingleFileViewerModel({
      file: baseFile,
      previewUrl: 'https://preview.example/FILE1',
      downloadUrl: 'https://download.example/FILE1'
    })

    expect(model.kind).toBe('DOCUMENT')
    expect(model.canPreview).toBe(true)
    expect(model.canDownload).toBe(true)
    expect(model.pdf).toBe(true)
  })

  it('does not preview office document inline', () => {
    const model = buildSingleFileViewerModel({
      file: {
        ...baseFile,
        originalName: 'contract.docx',
        contentType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      },
      previewUrl: 'https://preview.example/FILE2'
    })

    expect(model.canPreview).toBe(false)
  })
})
