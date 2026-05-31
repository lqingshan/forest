import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Department, Member } from '../../../shared/types'
import OrganizationMembersWorkspace from './OrganizationMembersWorkspace.vue'
import {
  addMember,
  disableMember,
  listDepartments,
  listMembers
} from '../../api/organization-api'

vi.mock('../../api/organization-api', () => ({
  activateMember: vi.fn(),
  addMember: vi.fn(),
  disableMember: vi.fn(),
  listDepartments: vi.fn(),
  listMembers: vi.fn()
}))

const member: Member = {
  memberId: 1,
  memberNo: 'MEM001',
  name: '张三',
  phone: '13800138000',
  departmentId: 1,
  status: 'ACTIVE'
}

const defaultDepartment: Department = {
  id: 1,
  departmentNo: 'DEP001',
  organizationId: 1,
  parentId: null,
  departmentName: '默认部门',
  defaultDepartment: true,
  sortOrder: 0,
  status: 'ACTIVE'
}

const salesDepartment: Department = {
  id: 2,
  departmentNo: 'DEP002',
  organizationId: 1,
  parentId: 1,
  departmentName: '销售部',
  defaultDepartment: false,
  sortOrder: 1,
  status: 'ACTIVE'
}

describe('OrganizationMembersWorkspace', () => {
  function rawOptionText(option: { element: Element }) {
    return option.element.textContent?.replace(/[\n\r]/g, '').replace(/^[ \t]+|[ \t]+$/g, '') ?? ''
  }

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('loads members and adds member', async () => {
    vi.mocked(listMembers).mockResolvedValue([member])
    vi.mocked(listDepartments).mockResolvedValue([defaultDepartment, salesDepartment])
    vi.mocked(addMember).mockResolvedValue(member)
    const wrapper = mount(OrganizationMembersWorkspace, {
      props: {
        organizationNo: 'ORG001'
      }
    })
    await flushPromises()

    expect(wrapper.text()).toContain('张三')
    const departmentOptions = wrapper.findAll('select option')
    expect(departmentOptions.map(rawOptionText)).toEqual(['默认部门', '　销售部'])
    expect(wrapper.find('select').element.value).toBe('1')

    await wrapper.find('input[placeholder="请输入员工手机号"]').setValue('13900139000')
    await wrapper.find('input[placeholder="请输入员工姓名"]').setValue('李四')
    await wrapper.find('input[type="password"]').setValue('secret')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(addMember).toHaveBeenCalledWith('ORG001', {
      phone: '13900139000',
      name: '李四',
      initialPassword: 'secret',
      departmentId: 1
    })
    expect(wrapper.emitted('changed')).toBeTruthy()
  })

  it('adds member with selected department id', async () => {
    vi.mocked(listMembers).mockResolvedValue([member])
    vi.mocked(listDepartments).mockResolvedValue([defaultDepartment, salesDepartment])
    vi.mocked(addMember).mockResolvedValue(member)
    const wrapper = mount(OrganizationMembersWorkspace, {
      props: {
        organizationNo: 'ORG001'
      }
    })
    await flushPromises()

    await wrapper.find('input[placeholder="请输入员工手机号"]').setValue('13900139000')
    await wrapper.find('input[placeholder="请输入员工姓名"]').setValue('李四')
    await wrapper.find('input[type="password"]').setValue('secret')
    await wrapper.find('select').setValue('2')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(addMember).toHaveBeenCalledWith('ORG001', {
      phone: '13900139000',
      name: '李四',
      initialPassword: 'secret',
      departmentId: 2
    })
  })

  it('disables active member', async () => {
    vi.mocked(listMembers).mockResolvedValue([member])
    vi.mocked(listDepartments).mockResolvedValue([defaultDepartment])
    vi.mocked(disableMember).mockResolvedValue({ ...member, status: 'DISABLED' })
    const wrapper = mount(OrganizationMembersWorkspace, {
      props: {
        organizationNo: 'ORG001'
      }
    })
    await flushPromises()

    await wrapper.find('button.workspace-button-soft').trigger('click')
    await flushPromises()

    expect(disableMember).toHaveBeenCalledWith('ORG001', 1)
  })
})
