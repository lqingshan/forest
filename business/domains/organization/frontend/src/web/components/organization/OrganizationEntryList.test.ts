import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import type { Organization } from '../../../shared/types'
import OrganizationEntryList from './OrganizationEntryList.vue'

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

describe('OrganizationEntryList', () => {
  it('renders organizations and emits selected organization number', async () => {
    const wrapper = mount(OrganizationEntryList, {
      props: {
        organizations: [organization],
        selectedOrganizationNo: 'ORG001'
      }
    })

    expect(wrapper.text()).toContain('CXC')
    expect(wrapper.text()).toContain('已认证')
    expect(wrapper.find('.organization-list__item').attributes('aria-label')).toBe('进入企业 CXC')

    await wrapper.find('.organization-list__item').trigger('click')

    expect(wrapper.emitted('select')?.[0]).toEqual(['ORG001'])
  })

  it('renders loading, empty, and error states', () => {
    expect(mount(OrganizationEntryList, {
      props: {
        organizations: [],
        loading: true
      }
    }).text()).toContain('加载中')

    expect(mount(OrganizationEntryList, {
      props: {
        organizations: []
      }
    }).text()).toContain('暂无企业，请先创建。')

    expect(mount(OrganizationEntryList, {
      props: {
        organizations: [],
        errorMessage: '加载失败'
      }
    }).text()).toContain('加载失败')
  })
})
