import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Department, Member } from '../shared/types'
import OrganizationMembersWorkspace from './OrganizationMembersWorkspace.vue'
import {
  addMember,
  disableMember,
  listDepartments,
  listMembers
} from './api'

vi.mock('./api', () => ({
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

describe('OrganizationMembersWorkspace', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('loads members and adds member', async () => {
    vi.mocked(listMembers).mockResolvedValue([member])
    vi.mocked(listDepartments).mockResolvedValue([department])
    vi.mocked(addMember).mockResolvedValue(member)
    const wrapper = mount(OrganizationMembersWorkspace, {
      props: {
        organizationNo: 'ORG001'
      }
    })
    await flushPromises()

    expect(wrapper.text()).toContain('张三')
    await wrapper.find('input[placeholder="请输入员工手机号"]').setValue('13900139000')
    await wrapper.find('input[placeholder="请输入员工姓名"]').setValue('李四')
    await wrapper.find('input[type="password"]').setValue('secret')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(addMember).toHaveBeenCalledWith('ORG001', {
      phone: '13900139000',
      name: '李四',
      initialPassword: 'secret',
      departmentId: null
    })
    expect(wrapper.emitted('changed')).toBeTruthy()
  })

  it('disables active member', async () => {
    vi.mocked(listMembers).mockResolvedValue([member])
    vi.mocked(listDepartments).mockResolvedValue([department])
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
