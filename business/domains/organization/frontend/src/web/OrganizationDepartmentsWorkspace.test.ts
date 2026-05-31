import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Department } from '../shared/types'
import OrganizationDepartmentsWorkspace from './OrganizationDepartmentsWorkspace.vue'
import { createDepartment, deleteDepartment, listDepartments } from './api'

vi.mock('./api', () => ({
  createDepartment: vi.fn(),
  deleteDepartment: vi.fn(),
  listDepartments: vi.fn()
}))

const department: Department = {
  id: 1,
  departmentNo: 'DEP001',
  organizationId: 1,
  parentId: null,
  departmentName: '默认部门',
  defaultDepartment: true,
  sortOrder: 0,
  status: 'ACTIVE'
}

const childDepartment: Department = {
  ...department,
  id: 2,
  departmentNo: 'DEP002',
  departmentName: '销售部',
  defaultDepartment: false
}

describe('OrganizationDepartmentsWorkspace', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('loads departments and creates department', async () => {
    vi.mocked(listDepartments).mockResolvedValue([department])
    vi.mocked(createDepartment).mockResolvedValue(childDepartment)
    const wrapper = mount(OrganizationDepartmentsWorkspace, {
      props: {
        organizationNo: 'ORG001'
      }
    })
    await flushPromises()

    expect(wrapper.text()).toContain('默认部门')
    await wrapper.find('input[placeholder="请输入部门名称"]').setValue('销售部')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(createDepartment).toHaveBeenCalledWith('ORG001', {
      parentId: null,
      departmentName: '销售部',
      sortOrder: 0
    })
    expect(wrapper.emitted('changed')).toBeTruthy()
  })

  it('deletes non-default department', async () => {
    vi.mocked(listDepartments).mockResolvedValue([childDepartment])
    vi.mocked(deleteDepartment).mockResolvedValue({ success: true })
    const wrapper = mount(OrganizationDepartmentsWorkspace, {
      props: {
        organizationNo: 'ORG001'
      }
    })
    await flushPromises()

    await wrapper.find('button.workspace-button-soft').trigger('click')
    await flushPromises()

    expect(deleteDepartment).toHaveBeenCalledWith('ORG001', 2)
  })
})
