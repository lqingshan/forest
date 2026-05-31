import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import UserStatusActions from './UserStatusActions.vue'
import type { UserManagementUser } from './types'

function buildUser(overrides: Partial<UserManagementUser> = {}): UserManagementUser {
  return {
    id: 7,
    name: 'Alice',
    avatar: null,
    avatarUrl: null,
    phone: null,
    email: null,
    status: 'ACTIVE',
    adminUser: true,
    user: true,
    createdTime: null,
    ...overrides
  }
}

describe('UserStatusActions', () => {
  it('allows freezing non-admin names even when adminUser defaults to true', async () => {
    const wrapper = mount(UserStatusActions, {
      props: {
        user: buildUser()
      }
    })

    const freezeButton = wrapper.find('.workspace-button-danger')
    expect(freezeButton.attributes('disabled')).toBeUndefined()

    await freezeButton.trigger('click')
    expect(wrapper.emitted('freeze')).toEqual([[7]])
  })

  it('disables freezing for the reserved admin name', () => {
    const wrapper = mount(UserStatusActions, {
      props: {
        user: buildUser({ name: 'admin' })
      }
    })

    expect(wrapper.find('.workspace-button-danger').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('admin 用户不能被冻结')
  })
})
