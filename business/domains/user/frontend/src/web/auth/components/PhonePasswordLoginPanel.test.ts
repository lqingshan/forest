import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import PhonePasswordLoginPanel from './PhonePasswordLoginPanel.vue'
import type { WebUserSession } from '../session-factory'

describe('PhonePasswordLoginPanel', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('shows field error when required credentials are missing', async () => {
    const session = {
      loginWithPassword: vi.fn()
    } as unknown as WebUserSession
    const wrapper = mount(PhonePasswordLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('form').trigger('submit')

    const alert = wrapper.find('[role="alert"]')
    expect(alert.exists()).toBe(true)
    expect(alert.text()).toBe('请填写手机号和密码')
    expect(session.loginWithPassword).not.toHaveBeenCalled()

    await wrapper.find('input[autocomplete="username"]').setValue('13800138000')

    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
  })

  it('logs in with explicit appCode and clientType from props', async () => {
    const user = {
      id: 1,
      name: '员工',
      avatar: null,
      avatarUrl: null,
      phone: '13800138000',
      email: null,
      status: 'ACTIVE',
      adminUser: false,
      user: true
    } as const
    const session = {
      loginWithPassword: vi.fn().mockResolvedValue(user)
    } as unknown as WebUserSession
    const wrapper = mount(PhonePasswordLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[autocomplete="username"]').setValue('13800138000')
    await wrapper.find('input[type="password"]').setValue('secret')
    await wrapper.find('form').trigger('submit')

    expect(session.loginWithPassword).toHaveBeenCalledWith('13800138000', 'secret', {
      savePassword: true
    })
    expect(wrapper.emitted('success')?.[0]).toEqual([user])
  })

  it('defaults to saving password', () => {
    const session = {
      loginWithPassword: vi.fn()
    } as unknown as WebUserSession
    const wrapper = mount(PhonePasswordLoginPanel, {
      props: {
        session
      }
    })

    expect((wrapper.find('input[type="checkbox"]').element as HTMLInputElement).checked).toBe(true)
  })

  it('logs in without saving password when remember checkbox is unchecked', async () => {
    const user = {
      id: 1,
      name: '员工',
      avatar: null,
      avatarUrl: null,
      phone: '13800138000',
      email: null,
      status: 'ACTIVE',
      adminUser: false,
      user: true
    } as const
    const session = {
      loginWithPassword: vi.fn().mockResolvedValue(user)
    } as unknown as WebUserSession
    const wrapper = mount(PhonePasswordLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[autocomplete="username"]').setValue('13800138000')
    await wrapper.find('input[type="password"]').setValue('secret')
    await wrapper.find('input[type="checkbox"]').setValue(false)
    await wrapper.find('form').trigger('submit')

    expect(session.loginWithPassword).toHaveBeenCalledWith('13800138000', 'secret', {
      savePassword: false
    })
    expect(wrapper.emitted('success')?.[0]).toEqual([user])
  })

  it('shows password login history when phone input is focused', async () => {
    const session = {
      loginWithPassword: vi.fn(),
      getLoginHistory: vi.fn().mockReturnValue({
        records: [
          { mode: 'phone_password', identifier: '13800138000', password: 'secret' }
        ]
      })
    } as unknown as WebUserSession
    const wrapper = mount(PhonePasswordLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[autocomplete="username"]').trigger('focus')

    expect(wrapper.find('.forest-password-login-panel__history').exists()).toBe(true)
    expect(wrapper.find('.forest-password-login-panel__history-item').text()).toContain('13800138000')
  })

  it('fills phone and password from saved password login history', async () => {
    const session = {
      loginWithPassword: vi.fn(),
      getLoginHistory: vi.fn().mockReturnValue({
        records: [
          { mode: 'phone_password', identifier: '13800138000', password: 'secret' }
        ]
      })
    } as unknown as WebUserSession
    const wrapper = mount(PhonePasswordLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[autocomplete="username"]').trigger('focus')
    await wrapper.find('.forest-password-login-panel__history-item').trigger('pointerdown')

    expect((wrapper.find('input[autocomplete="username"]').element as HTMLInputElement).value).toBe('13800138000')
    expect((wrapper.find('input[type="password"]').element as HTMLInputElement).value).toBe('secret')
    expect((wrapper.find('input[type="checkbox"]').element as HTMLInputElement).checked).toBe(true)
  })

  it('fills only phone from password login history without saved password', async () => {
    const session = {
      loginWithPassword: vi.fn(),
      getLoginHistory: vi.fn().mockReturnValue({
        records: [
          { mode: 'phone_password', identifier: '13800138000' }
        ]
      })
    } as unknown as WebUserSession
    const wrapper = mount(PhonePasswordLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[type="password"]').setValue('typed-secret')
    await wrapper.find('input[autocomplete="username"]').trigger('focus')
    await wrapper.find('.forest-password-login-panel__history-item').trigger('pointerdown')

    expect((wrapper.find('input[autocomplete="username"]').element as HTMLInputElement).value).toBe('13800138000')
    expect((wrapper.find('input[type="password"]').element as HTMLInputElement).value).toBe('')
    expect((wrapper.find('input[type="checkbox"]').element as HTMLInputElement).checked).toBe(false)
  })

  it('filters password login history by identifier input', async () => {
    const session = {
      loginWithPassword: vi.fn(),
      getLoginHistory: vi.fn().mockReturnValue({
        records: [
          { mode: 'phone_password', identifier: '13800138000', password: 'secret' },
          { mode: 'phone_password', identifier: '18257140000', password: 'another-secret' }
        ]
      })
    } as unknown as WebUserSession
    const wrapper = mount(PhonePasswordLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[autocomplete="username"]').trigger('focus')
    await wrapper.find('input[autocomplete="username"]').setValue('182')

    const historyItems = wrapper.findAll('.forest-password-login-panel__history-item')
    expect(historyItems).toHaveLength(1)
    expect(historyItems[0].text()).toContain('18257140000')
  })

  it('does not show password login history when there are no records', async () => {
    const session = {
      loginWithPassword: vi.fn(),
      getLoginHistory: vi.fn().mockReturnValue({ records: [] })
    } as unknown as WebUserSession
    const wrapper = mount(PhonePasswordLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[autocomplete="username"]').trigger('focus')

    expect(wrapper.find('.forest-password-login-panel__history').exists()).toBe(false)
  })
})
