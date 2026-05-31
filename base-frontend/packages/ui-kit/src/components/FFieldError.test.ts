import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import FFieldError from './FFieldError.vue'

describe('FFieldError', () => {
  it('renders a trimmed message as an alert', () => {
    const wrapper = mount(FFieldError, {
      props: {
        message: ' 验证码错误 '
      }
    })

    expect(wrapper.text()).toBe('验证码错误')
    expect(wrapper.attributes('role')).toBe('alert')
    expect(wrapper.classes()).toContain('f-field-error')
  })

  it.each([undefined, null, '', '   '])('does not render empty message %s', (message) => {
    const wrapper = mount(FFieldError, {
      props: {
        message
      }
    })

    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
  })

  it('passes through root attrs for field association and style overrides', () => {
    const wrapper = mount(FFieldError, {
      props: {
        message: '请选择头像'
      },
      attrs: {
        id: 'avatar-error',
        class: 'profile-form-error',
        style: '--forest-field-error-margin: 4px 0;',
        'data-testid': 'avatar-error'
      }
    })

    expect(wrapper.attributes('id')).toBe('avatar-error')
    expect(wrapper.attributes('data-testid')).toBe('avatar-error')
    expect(wrapper.classes()).toContain('profile-form-error')
    expect(wrapper.attributes('style')).toContain('--forest-field-error-margin: 4px 0;')
  })
})
