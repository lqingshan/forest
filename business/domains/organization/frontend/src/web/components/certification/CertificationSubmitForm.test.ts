import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { nextTick } from 'vue'
import CertificationSubmitForm from './CertificationSubmitForm.vue'

describe('CertificationSubmitForm', () => {
  it('emits submit payload from form fields', async () => {
    const wrapper = mount(CertificationSubmitForm)

    await wrapper.find('input[placeholder="请输入企业名称"]').setValue('CXC')
    await wrapper.find('input[placeholder="可重复，不做全局唯一限制"]').setValue('USCC')
    await wrapper.find('input[placeholder="请输入法人姓名"]').setValue('张三')
    await wrapper.find('input[placeholder="请输入联系人姓名"]').setValue('李四')
    await wrapper.find('input[placeholder="请输入联系人手机号"]').setValue('13800138000')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')?.[0]).toEqual([{
      companyName: 'CXC',
      unifiedSocialCreditCode: 'USCC',
      legalRepresentativeName: '张三',
      contactName: '李四',
      contactPhone: '13800138000'
    }])
  })

  it('emits selected upload file', async () => {
    const wrapper = mount(CertificationSubmitForm)
    const file = new File(['license'], 'license.pdf', { type: 'application/pdf' })
    const fileInput = wrapper.find('input[type="file"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: [file]
    })

    await fileInput.trigger('change')

    expect(wrapper.emitted('upload')?.[0]).toEqual([file])
  })

  it('renders upload state and can reset fields', async () => {
    const wrapper = mount(CertificationSubmitForm, {
      props: {
        uploading: true,
        licenseFileNo: 'FILE001',
        errorMessage: '提交失败'
      }
    })

    await wrapper.find('input[placeholder="请输入企业名称"]').setValue('CXC')
    ;(wrapper.vm as unknown as { reset: () => void }).reset()
    await nextTick()

    expect(wrapper.find('button').element.disabled).toBe(true)
    expect(wrapper.find('button').text()).toBe('上传执照中')
    expect(wrapper.text()).toContain('FILE001')
    expect(wrapper.text()).toContain('提交失败')
    expect((wrapper.find('input[placeholder="请输入企业名称"]').element as HTMLInputElement).value).toBe('')
  })
})
