import { describe, expect, it } from 'vitest'
import { inferContentType, validateFileSize } from './file-policy'

describe('file policy', () => {
  it('infers common content types', () => {
    expect(inferContentType('main.jpg')).toBe('image/jpeg')
    expect(inferContentType('contract.pdf')).toBe('application/pdf')
    expect(inferContentType('demo.mp4')).toBe('video/mp4')
    expect(inferContentType('intro.mp3')).toBe('audio/mpeg')
  })

  it('rejects files over category limit', () => {
    expect(() => validateFileSize('IMAGE', 11 * 1024 * 1024)).toThrow('文件大小超过限制')
    expect(() => validateFileSize('VIDEO', 50 * 1024 * 1024)).not.toThrow()
    expect(() => validateFileSize('AUDIO', 50 * 1024 * 1024)).not.toThrow()
  })
})
