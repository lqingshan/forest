import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Organization } from '../../../shared/types'
import OrganizationManagementWorkspace from './OrganizationManagementWorkspace.vue'
import { createOrganization } from '../../api/organization-api'

vi.mock('../../api/organization-api', () => ({
  createOrganization: vi.fn()
}))

const organization: Organization = {
  id: 1,
  organizationNo: 'ORG001',
  organizationName: 'CXC',
  status: 'ACTIVE',
  certificationStatus: 'NOT_SUBMITTED',
  currentCertificationId: null,
  ownerUserId: 100,
  createdTime: '2026-05-15T00:00:00'
}

describe('OrganizationManagementWorkspace', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders organizations and emits selection', async () => {
    const wrapper = mount(OrganizationManagementWorkspace, {
      props: {
        organizations: [organization],
        selectedOrganizationNo: 'ORG001'
      }
    })

    expect(wrapper.text()).toContain('CXC')
    expect(wrapper.text()).toContain('进入企业')
    expect(wrapper.find('.organization-list__item').attributes('aria-label')).toBe('进入企业 CXC')
    await wrapper.find('.organization-list__item').trigger('click')

    expect(wrapper.emitted('select')?.[0]).toEqual(['ORG001'])
  })

  it('creates organization and emits created result', async () => {
    vi.mocked(createOrganization).mockResolvedValue(organization)
    const wrapper = mount(OrganizationManagementWorkspace, {
      props: {
        organizations: []
      }
    })

    await wrapper.find('input').setValue('CXC')
    await wrapper.find('form').trigger('submit')

    expect(createOrganization).toHaveBeenCalledWith({ organizationName: 'CXC' })
    expect(wrapper.emitted('created')?.[0]).toEqual([organization])
  })
})
