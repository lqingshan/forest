import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Organization } from '../../../shared/types'
import OrganizationProfileWorkspace from './OrganizationProfileWorkspace.vue'
import { fetchOrganization, updateOrganization } from '../../api/organization-api'

vi.mock('../../api/organization-api', () => ({
  fetchOrganization: vi.fn(),
  updateOrganization: vi.fn()
}))

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

describe('OrganizationProfileWorkspace', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('shows empty organization hint when organizationNo is missing', () => {
    const wrapper = mount(OrganizationProfileWorkspace)

    expect(wrapper.text()).toContain('请先在企业入口页选择企业')
    expect(fetchOrganization).not.toHaveBeenCalled()
  })

  it('loads and updates organization profile', async () => {
    vi.mocked(fetchOrganization).mockResolvedValue(organization)
    vi.mocked(updateOrganization).mockResolvedValue({
      ...organization,
      organizationName: '新企业'
    })
    const wrapper = mount(OrganizationProfileWorkspace, {
      props: {
        organizationNo: 'ORG001',
        canUpdate: true
      }
    })
    await flushPromises()

    expect(fetchOrganization).toHaveBeenCalledWith('ORG001')
    expect(wrapper.text()).toContain('CXC')

    await wrapper.find('input[placeholder="请输入企业名称"]').setValue('新企业')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(updateOrganization).toHaveBeenCalledWith('ORG001', {
      organizationName: '新企业'
    })
    expect(wrapper.emitted('updated')?.[0]).toEqual([{
      ...organization,
      organizationName: '新企业'
    }])
  })
})
