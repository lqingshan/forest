import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Certification } from '../shared/types'
import OrganizationCertificationReviewWorkspace from './OrganizationCertificationReviewWorkspace.vue'
import {
  approveOrganizationCertification,
  listPendingOrganizationCertifications,
  rejectOrganizationCertification
} from './api'

vi.mock('./api', () => ({
  approveOrganizationCertification: vi.fn(),
  listPendingOrganizationCertifications: vi.fn(),
  rejectOrganizationCertification: vi.fn()
}))

const certification: Certification = {
  id: 1,
  certificationNo: 'CERT001',
  organizationId: 1,
  companyName: 'CXC',
  unifiedSocialCreditCode: 'USCC',
  legalRepresentativeName: '张三',
  businessLicenseFileNo: 'FILE001',
  contactName: '李四',
  contactPhone: '13800138000',
  status: 'PENDING',
  submittedByUserId: 100,
  reviewedByUserId: null,
  reviewedTime: null,
  reviewRemark: null,
  createdTime: '2026-05-15T00:00:00'
}

describe('OrganizationCertificationReviewWorkspace', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('loads pending certifications and approves certification', async () => {
    vi.mocked(listPendingOrganizationCertifications).mockResolvedValue([certification])
    vi.mocked(approveOrganizationCertification).mockResolvedValue({ ...certification, status: 'APPROVED' })
    const wrapper = mount(OrganizationCertificationReviewWorkspace)
    await flushPromises()

    expect(wrapper.text()).toContain('CXC')
    await wrapper.find('input').setValue('资料完整')
    await wrapper.find('button.workspace-button-success').trigger('click')
    await flushPromises()

    expect(approveOrganizationCertification).toHaveBeenCalledWith(1, { reviewRemark: '资料完整' })
    expect(wrapper.emitted('changed')).toBeTruthy()
  })

  it('rejects certification', async () => {
    vi.mocked(listPendingOrganizationCertifications).mockResolvedValue([certification])
    vi.mocked(rejectOrganizationCertification).mockResolvedValue({ ...certification, status: 'REJECTED' })
    const wrapper = mount(OrganizationCertificationReviewWorkspace)
    await flushPromises()

    await wrapper.find('button.workspace-button-danger').trigger('click')
    await flushPromises()

    expect(rejectOrganizationCertification).toHaveBeenCalledWith(1, { reviewRemark: null })
  })
})
