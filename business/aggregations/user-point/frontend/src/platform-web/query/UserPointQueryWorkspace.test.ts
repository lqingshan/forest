import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import UserPointQueryWorkspace from './UserPointQueryWorkspace.vue'

const {
  fetchUserPointPage
} = vi.hoisted(() => ({
  fetchUserPointPage: vi.fn()
}))

vi.mock('./api', () => ({
  fetchUserPointPage
}))

const flushPromises = () => new Promise((resolve) => setTimeout(resolve, 0))

describe('UserPointQueryWorkspace', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('allows empty search and requests the first page', async () => {
    fetchUserPointPage.mockResolvedValue({
      content: [
        {
          user: {
            id: 7,
            name: 'Alice',
            avatar: null,
            phone: '13800138000',
            email: 'alice@forest.example',
            status: 'ACTIVE',
            adminUser: true,
            user: true
          },
          points: {
            balance: 12,
            totalIncome: 12,
            totalSpend: 0,
            updatedAt: '2026-04-14T12:00:00'
          }
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 20,
      number: 0
    })

    const wrapper = shallowMount(UserPointQueryWorkspace)

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(fetchUserPointPage).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      name: undefined,
      phone: undefined,
      email: undefined
    })
    expect(wrapper.text()).toContain('Alice')
    expect(wrapper.text()).toContain('2026-04-14 12:00')
  })

  it('rejects short phone or email before sending requests', async () => {
    const wrapper = shallowMount(UserPointQueryWorkspace)

    const inputs = wrapper.findAll('input')
    await inputs[1].setValue('1')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(fetchUserPointPage).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('手机号或邮箱至少输入 2 个字符')
  })

  it('emits the selected user id instead of mutating app routes', async () => {
    fetchUserPointPage.mockResolvedValue({
      content: [
        {
          user: {
            id: 9,
            name: 'Bob',
            avatar: null,
            phone: null,
            email: null,
            status: 'ACTIVE',
            adminUser: true,
            user: true
          },
          points: {
            balance: 3,
            totalIncome: 3,
            totalSpend: 0,
            updatedAt: '2026-04-14T10:00:00'
          }
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 20,
      number: 0
    })

    const wrapper = shallowMount(UserPointQueryWorkspace)

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()
    await wrapper.find('.workspace-table__button').trigger('click')

    expect(wrapper.emitted('select-user')).toEqual([[9]])
  })

  it('emits selection-missing when the controlled user is not in the loaded page', async () => {
    fetchUserPointPage.mockResolvedValue({
      content: [
        {
          user: {
            id: 9,
            name: 'Bob',
            avatar: null,
            phone: null,
            email: null,
            status: 'ACTIVE',
            adminUser: true,
            user: true
          },
          points: {
            balance: 3,
            totalIncome: 3,
            totalSpend: 0,
            updatedAt: '2026-04-14T10:00:00'
          }
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 20,
      number: 0
    })

    const wrapper = shallowMount(UserPointQueryWorkspace, {
      props: {
        selectedUserId: 10
      }
    })

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.emitted('selection-missing')).toEqual([[]])
  })
})
