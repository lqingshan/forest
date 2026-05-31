export const FILE_UPLOAD_COPY = {
  IMAGE: {
    title: '上传图片',
    buttonText: '选择图片',
    uploadingText: '图片上传中'
  },
  DOCUMENT: {
    title: '上传文档',
    buttonText: '选择文档',
    uploadingText: '文档上传中'
  },
  VIDEO: {
    title: '上传视频',
    buttonText: '选择视频',
    uploadingText: '视频上传中'
  },
  AUDIO: {
    title: '上传音频',
    buttonText: '选择音频',
    uploadingText: '音频上传中'
  },
  successText: '上传成功',
  failedText: '上传失败，请重试'
}

export function getFileUploadErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : FILE_UPLOAD_COPY.failedText
}
