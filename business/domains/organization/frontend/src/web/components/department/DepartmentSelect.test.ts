import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import type { Department } from '../../../shared/types'
import DepartmentSelect from './DepartmentSelect.vue'

const departments: Department[] = [
  {
    id: 1,
    departmentNo: 'DEP001',
    organizationId: 1,
    parentId: null,
    departmentName: '默认部门',
    defaultDepartment: true,
    sortOrder: 0,
    status: 'ACTIVE'
  },
  {
    id: 2,
    departmentNo: 'DEP002',
    organizationId: 1,
    parentId: 1,
    departmentName: '销售部',
    defaultDepartment: false,
    sortOrder: 1,
    status: 'ACTIVE'
  },
  {
    id: 3,
    departmentNo: 'DEP003',
    organizationId: 1,
    parentId: 2,
    departmentName: '销售一部',
    defaultDepartment: false,
    sortOrder: 2,
    status: 'ACTIVE'
  }
]

describe('DepartmentSelect', () => {
  function rawOptionText(option: { element: Element }) {
    return option.element.textContent?.replace(/[\n\r]/g, '').replace(/^[ \t]+|[ \t]+$/g, '') ?? ''
  }

  it('renders department options with hierarchy indentation', () => {
    const wrapper = mount(DepartmentSelect, {
      props: {
        departments,
        modelValue: '1'
      }
    })

    expect(wrapper.findAll('option').map(rawOptionText)).toEqual([
      '默认部门',
      '　销售部',
      '　　销售一部'
    ])
  })

  it('emits model update when selected value changes', async () => {
    const wrapper = mount(DepartmentSelect, {
      props: {
        departments,
        modelValue: '1'
      }
    })

    await wrapper.find('select').setValue('2')

    expect(wrapper.emitted('update:modelValue')).toEqual([[ '2' ]])
  })

  it('shows empty disabled option when there are no departments', () => {
    const wrapper = mount(DepartmentSelect, {
      props: {
        departments: [],
        modelValue: ''
      }
    })

    expect(wrapper.find('select').element.disabled).toBe(true)
    expect(wrapper.find('option').text()).toBe('暂无部门')
  })
})
