import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Organization } from '../shared/types'
import OrganizationManagementWorkspace from './OrganizationManagementWorkspace.vue'
import { createOrganization } from './api'

vi.mock('./api', () => ({
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
