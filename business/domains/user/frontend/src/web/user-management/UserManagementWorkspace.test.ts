import { mount } from '@vue/test-utils'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import UserManagementWorkspace from './UserManagementWorkspace.vue'

const {
  fetchUsers,
  fetchUser,
  freezeUser,
  activateUser
} = vi.hoisted(() => ({
  fetchUsers: vi.fn(),
  fetchUser: vi.fn(),
  freezeUser: vi.fn(),
  activateUser: vi.fn()
}))

vi.mock('./api', () => ({
  fetchUsers,
  fetchUser,
  freezeUser,
  activateUser
}))

const flushPromises = () => new Promise((resolve) => setTimeout(resolve, 0))

describe('UserManagementWorkspace', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('emits view-point when the selected user requests point lookup', async () => {
    fetchUsers.mockResolvedValue({
      content: [
        {
          id: 7,
          name: 'Alice',
          avatar: null,
          avatarUrl: null,
          phone: null,
          email: null,
          status: 'ACTIVE',
          createdTime: null,
          adminUser: true,
          user: true
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 20,
      number: 0
    })
    fetchUser.mockResolvedValue({
      id: 7,
      name: 'Alice',
      avatar: null,
      avatarUrl: null,
      phone: null,
      email: null,
      status: 'ACTIVE',
      createdTime: null,
      adminUser: true,
      user: true
    })

    const wrapper = mount(UserManagementWorkspace)
    await flushPromises()

    await wrapper.find('.workspace-table__button').trigger('click')
    await flushPromises()

    const buttons = wrapper.findAll('button')
    const viewPointsButton = buttons.find((button) => button.text() === '查看积分')
    expect(viewPointsButton).toBeTruthy()

    await viewPointsButton!.trigger('click')
    expect(wrapper.emitted('view-point')).toEqual([[7]])
  })
})
