import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { nextTick } from 'vue'
import CreateOrganizationForm from './CreateOrganizationForm.vue'

describe('CreateOrganizationForm', () => {
  it('emits submitted organization name', async () => {
    const wrapper = mount(CreateOrganizationForm)

    await wrapper.find('input').setValue('CXC')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')?.[0]).toEqual(['CXC'])
  })

  it('can reset input value after container creates organization successfully', async () => {
    const wrapper = mount(CreateOrganizationForm)

    await wrapper.find('input').setValue('CXC')
    ;(wrapper.vm as unknown as { reset: () => void }).reset()
    await nextTick()

    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('')
  })

  it('disables button while submitting', () => {
    const wrapper = mount(CreateOrganizationForm, {
      props: {
        submitting: true
      }
    })

    expect(wrapper.find('button').element.disabled).toBe(true)
    expect(wrapper.find('button').text()).toBe('创建中')
  })
})
