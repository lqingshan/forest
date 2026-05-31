import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import PhoneSmsLoginPanel from './PhoneSmsLoginPanel.vue'
import type { WebUserSession } from '../session-factory'

vi.mock('@forest/verification/web/sms', () => ({
  SmsCodeField: {
    props: ['modelValue', 'phone', 'appCode', 'clientType', 'accessScope', 'disabled'],
    emits: ['update:modelValue'],
    template: `<input class="mock-sms-code-field" :value="modelValue" @input="$emit('update:modelValue', $event.target.value)" />`
  }
}))

describe('PhoneSmsLoginPanel', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('requires phone and sms code before submitting', async () => {
    const session = createTestSession()
    const wrapper = mount(PhoneSmsLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('form').trigger('submit')

    const alert = wrapper.find('[role="alert"]')
    expect(alert.exists()).toBe(true)
    expect(alert.text()).toBe('请输入手机号')
    expect(session.loginWithPhoneSms).not.toHaveBeenCalled()

    await wrapper.find('input[type="tel"]').setValue('13800138000')

    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
  })

  it('requires sms code before submitting', async () => {
    const session = createTestSession()
    const wrapper = mount(PhoneSmsLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[type="tel"]').setValue('13800138000')
    await wrapper.find('form').trigger('submit')

    const alert = wrapper.find('[role="alert"]')
    expect(alert.exists()).toBe(true)
    expect(alert.text()).toBe('请输入验证码')
    expect(session.loginWithPhoneSms).not.toHaveBeenCalled()

    await wrapper.find('.mock-sms-code-field').setValue('121314')

    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
  })

  it('logs in with phone sms code and emits success', async () => {
    const user = {
      id: 1,
      name: '用户',
      avatar: null,
      avatarUrl: null,
      phone: '13800138000',
      email: null,
      status: 'ACTIVE',
      adminUser: false,
      user: true
    } as const
    const session = createTestSession()
    vi.mocked(session.loginWithPhoneSms).mockResolvedValue(user)
    const wrapper = mount(PhoneSmsLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[type="tel"]').setValue('13800138000')
    await wrapper.find('.mock-sms-code-field').setValue('121314')
    await wrapper.find('form').trigger('submit')

    expect(session.loginWithPhoneSms).toHaveBeenCalledWith('13800138000', '121314')
    expect(wrapper.emitted('success')?.[0]).toEqual([user])
  })

  it('shows phone sms login history when phone input is focused', async () => {
    const session = createTestSession([
      { mode: 'phone_sms', identifier: '13800138000' }
    ])
    const wrapper = mount(PhoneSmsLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[type="tel"]').trigger('focus')

    expect(wrapper.find('.forest-phone-sms-login-panel__history').exists()).toBe(true)
    expect(wrapper.find('.forest-phone-sms-login-panel__history-item').text()).toContain('13800138000')
  })

  it('fills phone from phone sms login history without changing sms code', async () => {
    const session = createTestSession([
      { mode: 'phone_sms', identifier: '13800138000' }
    ])
    const wrapper = mount(PhoneSmsLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('.mock-sms-code-field').setValue('121314')
    await wrapper.find('input[type="tel"]').trigger('focus')
    await wrapper.find('.forest-phone-sms-login-panel__history-item').trigger('pointerdown')

    expect((wrapper.find('input[type="tel"]').element as HTMLInputElement).value).toBe('13800138000')
    expect((wrapper.find('.mock-sms-code-field').element as HTMLInputElement).value).toBe('121314')
  })

  it('filters phone sms login history by identifier input', async () => {
    const session = createTestSession([
      { mode: 'phone_sms', identifier: '13800138000' },
      { mode: 'phone_sms', identifier: '18257140000' }
    ])
    const wrapper = mount(PhoneSmsLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[type="tel"]').trigger('focus')
    await wrapper.find('input[type="tel"]').setValue('182')

    const historyItems = wrapper.findAll('.forest-phone-sms-login-panel__history-item')
    expect(historyItems).toHaveLength(1)
    expect(historyItems[0].text()).toContain('18257140000')
  })

  it('does not show phone sms login history when there are no records', async () => {
    const session = createTestSession()
    const wrapper = mount(PhoneSmsLoginPanel, {
      props: {
        session
      }
    })

    await wrapper.find('input[type="tel"]').trigger('focus')

    expect(wrapper.find('.forest-phone-sms-login-panel__history').exists()).toBe(false)
  })
})

function createTestSession(records: Array<{ mode: 'phone_sms'; identifier: string }> = []) {
  return {
    appCode: 'cxc-commerce-buyer-mobile-h5',
    clientType: 'MOBILE_H5',
    accessScope: 'CLIENT',
    loginWithPhoneSms: vi.fn(),
    getLoginHistory: vi.fn().mockReturnValue({ records })
  } as unknown as WebUserSession
}
