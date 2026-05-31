import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import type { Certification } from '../../../shared/types'
import CertificationLatestCard from './CertificationLatestCard.vue'

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
  status: 'APPROVED',
  submittedByUserId: 100,
  reviewedByUserId: 200,
  reviewedTime: '2026-05-15T00:00:00',
  reviewRemark: '通过',
  createdTime: '2026-05-15T00:00:00'
}

describe('CertificationLatestCard', () => {
  it('renders latest certification fields', () => {
    const wrapper = mount(CertificationLatestCard, {
      props: {
        certification
      }
    })

    expect(wrapper.text()).toContain('CXC')
    expect(wrapper.text()).toContain('USCC')
    expect(wrapper.text()).toContain('FILE001')
    expect(wrapper.text()).toContain('已通过')
    expect(wrapper.text()).toContain('通过')
  })

  it('renders empty state when certification is missing', () => {
    const wrapper = mount(CertificationLatestCard, {
      props: {
        certification: null
      }
    })

    expect(wrapper.text()).toContain('暂无认证记录。')
  })
})
