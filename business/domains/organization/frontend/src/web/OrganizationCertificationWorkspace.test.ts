import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Certification } from '../shared/types'
import OrganizationCertificationWorkspace from './OrganizationCertificationWorkspace.vue'
import { fetchLatestCertification, submitCertification } from './api'
import { uploadWebFile } from '@forest/file/web/upload'

vi.mock('./api', () => ({
  fetchLatestCertification: vi.fn(),
  submitCertification: vi.fn()
}))

vi.mock('@forest/file/web/upload', () => ({
  uploadWebFile: vi.fn()
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

describe('OrganizationCertificationWorkspace', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('shows empty organization hint when organizationNo is missing', () => {
    const wrapper = mount(OrganizationCertificationWorkspace)

    expect(wrapper.text()).toContain('请先在企业入口页选择企业')
  })

  it('loads latest certification, uploads license and submits certification', async () => {
    vi.mocked(fetchLatestCertification).mockResolvedValue(null)
    vi.mocked(uploadWebFile).mockResolvedValue({ fileNo: 'FILE001' } as never)
    vi.mocked(submitCertification).mockResolvedValue(certification)
    const wrapper = mount(OrganizationCertificationWorkspace, {
      props: {
        organizationNo: 'ORG001'
      }
    })
    await flushPromises()

    await wrapper.find('input[placeholder="请输入企业名称"]').setValue('CXC')
    await wrapper.find('input[placeholder="可重复，不做全局唯一限制"]').setValue('USCC')
    await wrapper.find('input[placeholder="请输入法人姓名"]').setValue('张三')
    await wrapper.find('input[placeholder="请输入联系人姓名"]').setValue('李四')
    await wrapper.find('input[placeholder="请输入联系人手机号"]').setValue('13800138000')
    const fileInput = wrapper.find('input[type="file"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: [new File(['license'], 'license.pdf', { type: 'application/pdf' })]
    })
    await fileInput.trigger('change')
    await flushPromises()
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(uploadWebFile).toHaveBeenCalled()
    expect(submitCertification).toHaveBeenCalledWith('ORG001', {
      companyName: 'CXC',
      unifiedSocialCreditCode: 'USCC',
      legalRepresentativeName: '张三',
      contactName: '李四',
      contactPhone: '13800138000',
      businessLicenseFileNo: 'FILE001'
    })
    expect(wrapper.emitted('submitted')?.[0]).toEqual([certification])
  })
})
