import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import OrganizationProfileEditForm from './OrganizationProfileEditForm.vue'

describe('OrganizationProfileEditForm', () => {
  it('emits model update and save event', async () => {
    const wrapper = mount(OrganizationProfileEditForm, {
      props: {
        modelValue: 'CXC'
      }
    })

    await wrapper.find('input[placeholder="请输入企业名称"]').setValue('新企业')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['新企业'])
    expect(wrapper.emitted('save')).toHaveLength(1)
  })

  it('disables save button while submitting', () => {
    const wrapper = mount(OrganizationProfileEditForm, {
      props: {
        modelValue: 'CXC',
        submitting: true
      }
    })

    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('保存中')
  })
})
