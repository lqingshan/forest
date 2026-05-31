import { afterEach, describe, expect, it, vi } from 'vitest'
import { clientHttp, type HttpClient } from '@forest/http-client'
import {
  completeUploadSession,
  createDownloadUrls,
  createPreviewUrls,
  createUploadSession,
  deleteFile,
  fetchFile
} from './api'

describe('file api', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('creates upload session', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ fileNo: 'FILE1' } as never)

    await createUploadSession({
      originalName: 'main.jpg',
      contentType: 'image/jpeg',
      fileCategory: 'IMAGE',
      sizeBytes: 1024
    })

    expect(postSpy).toHaveBeenCalledWith('/api/client/file/upload-session', {
      originalName: 'main.jpg',
      contentType: 'image/jpeg',
      fileCategory: 'IMAGE',
      sizeBytes: 1024
    })
  })

  it('completes upload session', async () => {
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue({ fileNo: 'FILE1' } as never)

    await completeUploadSession('FUS1')

    expect(postSpy).toHaveBeenCalledWith('/api/client/file/upload-session/FUS1/complete')
  })

  it('fetches file and download urls', async () => {
    const getSpy = vi.spyOn(clientHttp, 'get').mockResolvedValue({ fileNo: 'FILE1' } as never)
    const postSpy = vi.spyOn(clientHttp, 'post').mockResolvedValue([] as never)

    await fetchFile('FILE1')
    await createDownloadUrls(['FILE1'])
    await createPreviewUrls(['FILE1'])

    expect(getSpy).toHaveBeenCalledWith('/api/client/file/FILE1')
    expect(postSpy).toHaveBeenCalledWith('/api/client/file/download-url', { fileNos: ['FILE1'] })
    expect(postSpy).toHaveBeenCalledWith('/api/client/file/preview-url', { fileNos: ['FILE1'] })
  })

  it('deletes file', async () => {
    const deleteSpy = vi.spyOn(clientHttp, 'delete').mockResolvedValue(undefined as never)

    await deleteFile('FILE1')

    expect(deleteSpy).toHaveBeenCalledWith('/api/client/file/FILE1')
  })

  it('can use an injected http client', async () => {
    const httpClient = createMockHttpClient()
    vi.mocked(httpClient.post).mockResolvedValue({ fileNo: 'FILE1' } as never)

    await createUploadSession({
      originalName: 'avatar.jpg',
      contentType: 'image/jpeg',
      fileCategory: 'IMAGE',
      sizeBytes: 2048
    }, {
      httpClient,
      scope: 'platform'
    })
    await completeUploadSession('FUS1', {
      httpClient,
      scope: 'platform'
    })

    expect(httpClient.post).toHaveBeenCalledWith('/api/platform/file/upload-session', {
      originalName: 'avatar.jpg',
      contentType: 'image/jpeg',
      fileCategory: 'IMAGE',
      sizeBytes: 2048
    })
    expect(httpClient.post).toHaveBeenCalledWith('/api/platform/file/upload-session/FUS1/complete')
  })
})

function createMockHttpClient(): HttpClient {
  return {
    get: vi.fn(),
    delete: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn()
  }
}
