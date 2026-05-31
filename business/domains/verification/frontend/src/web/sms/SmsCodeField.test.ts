import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { sendSmsCode } from '../../shared'
import SmsCodeField from './SmsCodeField.vue'

vi.mock('../../shared', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../shared')>()
  return {
    ...actual,
    sendSmsCode: vi.fn()
  }
})

describe('SmsCodeField', () => {
  afterEach(() => {
    vi.clearAllMocks()
    vi.useRealTimers()
  })

  it('emits normalized code without requiring sms send first', async () => {
    const wrapper = mount(SmsCodeField, {
      props: {
        phone: '13800138000',
        modelValue: '',
        appCode: 'cxc-commerce',
        clientType: 'MOBILE_H5',
        accessScope: 'CLIENT'
      }
    })

    await wrapper.find('input').setValue('12a314')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['12314'])
    expect(sendSmsCode).not.toHaveBeenCalled()
  })

  it('sends sms code and starts cooldown', async () => {
    vi.useFakeTimers()
    vi.mocked(sendSmsCode).mockResolvedValue({ phone: '13800138000', ttlSeconds: 2 })
    const wrapper = mount(SmsCodeField, {
      props: {
        phone: '13800138000',
        modelValue: '',
        appCode: 'cxc-commerce',
        clientType: 'MOBILE_H5',
        accessScope: 'CLIENT'
      }
    })

    await wrapper.find('button').trigger('click')

    expect(sendSmsCode).toHaveBeenCalledWith({
      phone: '13800138000',
      clientType: 'MOBILE_H5',
      appCode: 'cxc-commerce',
      accessScope: 'CLIENT'
    })
    expect(wrapper.emitted('sent')?.[0]).toEqual([{ phone: '13800138000', cooldownSeconds: 2 }])
    expect(wrapper.find('button').text()).toBe('2s')
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()

    vi.advanceTimersByTime(2000)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('button').text()).toBe('获取验证码')
  })

  it('does not use code ttl minutes as send cooldown', async () => {
    vi.useFakeTimers()
    vi.mocked(sendSmsCode).mockResolvedValue({ phone: '13800138000', ttlMinutes: 5 })
    const wrapper = mount(SmsCodeField, {
      props: {
        phone: '13800138000',
        modelValue: '',
        appCode: 'cxc-commerce',
        clientType: 'MOBILE_H5',
        accessScope: 'CLIENT'
      }
    })

    await wrapper.find('button').trigger('click')

    expect(wrapper.emitted('sent')?.[0]).toEqual([{ phone: '13800138000', cooldownSeconds: 60 }])
    expect(wrapper.find('button').text()).toBe('60s')
  })

  it('shows error when phone is blank', async () => {
    const wrapper = mount(SmsCodeField, {
      props: {
        phone: ' ',
        modelValue: '',
        appCode: 'cxc-commerce',
        clientType: 'MOBILE_H5',
        accessScope: 'CLIENT'
      }
    })

    await wrapper.find('button').trigger('click')

    const alert = wrapper.find('[role="alert"]')
    expect(alert.exists()).toBe(true)
    expect(alert.text()).toBe('请输入手机号')
    expect(wrapper.emitted('senderror')?.[0]).toEqual([{ message: '请输入手机号' }])
    expect(sendSmsCode).not.toHaveBeenCalled()

    await wrapper.setProps({ phone: '13800138000' })

    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
  })

  it('shows field error when sms send fails', async () => {
    vi.mocked(sendSmsCode).mockRejectedValue(new Error('验证码发送过于频繁'))
    const wrapper = mount(SmsCodeField, {
      props: {
        phone: '13800138000',
        modelValue: '',
        appCode: 'cxc-commerce',
        clientType: 'MOBILE_H5',
        accessScope: 'CLIENT'
      }
    })

    await wrapper.find('button').trigger('click')

    const alert = wrapper.find('[role="alert"]')
    expect(alert.exists()).toBe(true)
    expect(alert.text()).toBe('验证码发送过于频繁')
    expect(wrapper.emitted('senderror')?.[0]).toEqual([{ message: '验证码发送过于频繁' }])

    await wrapper.find('input').setValue('123456')

    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
  })

  it('clears previous error before sending sms code again', async () => {
    vi.mocked(sendSmsCode)
      .mockRejectedValueOnce(new Error('验证码发送过于频繁'))
      .mockResolvedValueOnce({ phone: '13800138000', ttlSeconds: 2 })
    const wrapper = mount(SmsCodeField, {
      props: {
        phone: '13800138000',
        modelValue: '',
        appCode: 'cxc-commerce',
        clientType: 'MOBILE_H5',
        accessScope: 'CLIENT'
      }
    })

    await wrapper.find('button').trigger('click')
    expect(wrapper.find('[role="alert"]').text()).toBe('验证码发送过于频繁')

    await wrapper.find('button').trigger('click')

    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
    expect(wrapper.emitted('sent')?.[0]).toEqual([{ phone: '13800138000', cooldownSeconds: 2 }])
  })
})
