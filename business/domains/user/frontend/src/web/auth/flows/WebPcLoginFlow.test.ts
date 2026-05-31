import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import WebPcLoginFlow from './WebPcLoginFlow.vue'
import type { WebUserSession } from '../session-factory'

vi.mock('../components/PhonePasswordLoginPanel.vue', () => ({
  default: {
    props: ['session', 'title', 'submitText'],
    emits: ['success'],
    template: '<button class="mock-password-login" @click="$emit(\'success\', user)">password {{ title }} {{ submitText }}</button>',
    data: () => ({
      user: {
        id: 1,
        name: '用户',
        avatar: null,
        avatarUrl: null,
        phone: '13800138000',
        email: null,
        status: 'ACTIVE',
        adminUser: false,
        user: true
      }
    })
  }
}))

vi.mock('../components/PhoneSmsLoginPanel.vue', () => ({
  default: {
    props: ['session', 'title', 'submitText'],
    emits: ['success'],
    template: '<button class="mock-sms-login" @click="$emit(\'success\', user)">sms {{ title }} {{ submitText }}</button>',
    data: () => ({
      user: {
        id: 2,
        name: '短信用户',
        avatar: null,
        avatarUrl: null,
        phone: '13800138001',
        email: null,
        status: 'ACTIVE',
        adminUser: false,
        user: true
      }
    })
  }
}))

describe('WebPcLoginFlow', () => {
  it('defaults to phone password login and emits success', async () => {
    const wrapper = mount(WebPcLoginFlow, {
      props: {
        session: {} as WebUserSession
      }
    })

    expect(wrapper.find('.mock-password-login').exists()).toBe(true)
    expect(wrapper.text()).toContain('手机号密码登录')

    await wrapper.find('.mock-password-login').trigger('click')

    expect(wrapper.emitted('success')?.[0]?.[0]).toMatchObject({ id: 1 })
  })

  it('switches to phone sms login', async () => {
    const wrapper = mount(WebPcLoginFlow, {
      props: {
        session: {} as WebUserSession,
        smsTitle: '验证码登录'
      }
    })

    await wrapper.findAll('.forest-web-pc-login-panel__tab')[1].trigger('click')

    expect(wrapper.find('.mock-sms-login').exists()).toBe(true)
    expect(wrapper.text()).toContain('验证码登录')
  })
})
