import { mount } from '@vue/test-utils'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import LeadManagementWorkspace from './LeadManagementWorkspace.vue'

const {
  fetchLeadPlatformItems,
  fetchLeadPlatformItem,
  createLeadPlatformItem,
  updateLeadPlatformItem,
  deleteLeadPlatformItem
} = vi.hoisted(() => ({
  fetchLeadPlatformItems: vi.fn(),
  fetchLeadPlatformItem: vi.fn(),
  createLeadPlatformItem: vi.fn(),
  updateLeadPlatformItem: vi.fn(),
  deleteLeadPlatformItem: vi.fn()
}))

vi.mock('./api', () => ({
  fetchLeadPlatformItems,
  fetchLeadPlatformItem,
  createLeadPlatformItem,
  updateLeadPlatformItem,
  deleteLeadPlatformItem
}))

const flushPromises = () => new Promise((resolve) => setTimeout(resolve, 0))

describe('LeadManagementWorkspace', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('disables the form while lead details are still loading', async () => {
    fetchLeadPlatformItems.mockResolvedValue({
      content: [
        {
          id: 3,
          sourceType: 'MANUAL',
          keywords: 'tea',
          name: 'Tea Supplier',
          category: 'Food',
          country: 'CN',
          phone: null,
          email: 'tea@example.com',
          website: null,
          intro: null
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 20,
      number: 0
    })

    let resolveLead: ((value: unknown) => void) | undefined
    fetchLeadPlatformItem.mockReturnValue(new Promise((resolve) => {
      resolveLead = resolve
    }))

    const wrapper = mount(LeadManagementWorkspace)
    await flushPromises()

    await wrapper.find('.workspace-table__button').trigger('click')
    await flushPromises()

    expect(wrapper.find('input[name="name"]').attributes('disabled')).toBeDefined()

    resolveLead?.({
      id: 3,
      sourceType: 'MANUAL',
      keywords: 'tea',
      name: 'Tea Supplier',
      category: 'Food',
      country: 'CN',
      phone: null,
      email: 'tea@example.com',
      website: null,
      intro: 'Loaded'
    })
    await flushPromises()

    expect(wrapper.find('input[name="name"]').attributes('disabled')).toBeUndefined()
  })
})
