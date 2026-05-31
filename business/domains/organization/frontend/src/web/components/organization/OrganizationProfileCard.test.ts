import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import type { Organization } from '../../../shared/types'
import OrganizationProfileCard from './OrganizationProfileCard.vue'

const organization: Organization = {
  id: 1,
  organizationNo: 'ORG001',
  organizationName: 'CXC',
  status: 'ACTIVE',
  certificationStatus: 'APPROVED',
  currentCertificationId: null,
  ownerUserId: 100,
  createdTime: '2026-05-15T00:00:00'
}

describe('OrganizationProfileCard', () => {
  it('renders organization profile fields', () => {
    const wrapper = mount(OrganizationProfileCard, {
      props: {
        organization
      }
    })

    expect(wrapper.text()).toContain('CXC')
    expect(wrapper.text()).toContain('ORG001')
    expect(wrapper.text()).toContain('启用')
    expect(wrapper.text()).toContain('已认证')
  })
})
