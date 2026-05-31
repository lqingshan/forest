import { describe, expect, it } from 'vitest'
import {
  certificationReviewStatusText,
  memberStatusText,
  organizationCertificationStatusText,
  organizationStatusText
} from './status-text'

describe('organization status text', () => {
  it('maps organization status to display text', () => {
    expect(organizationStatusText('ACTIVE')).toBe('启用')
    expect(organizationStatusText('DISABLED')).toBe('禁用')
  })

  it('maps organization certification status to display text', () => {
    expect(organizationCertificationStatusText('NOT_SUBMITTED')).toBe('未认证')
    expect(organizationCertificationStatusText('PENDING')).toBe('待审核')
    expect(organizationCertificationStatusText('APPROVED')).toBe('已认证')
    expect(organizationCertificationStatusText('REJECTED')).toBe('已驳回')
  })

  it('maps certification review status to display text', () => {
    expect(certificationReviewStatusText('PENDING')).toBe('待审核')
    expect(certificationReviewStatusText('APPROVED')).toBe('已通过')
    expect(certificationReviewStatusText('REJECTED')).toBe('已驳回')
  })

  it('maps member status to display text', () => {
    expect(memberStatusText('ACTIVE')).toBe('启用')
    expect(memberStatusText('DISABLED')).toBe('停用')
  })
})
