import { describe, expect, it } from 'vitest'
import type { Department } from '../../../shared/types'
import {
  buildDepartmentOptions,
  buildDepartmentTree,
  flattenDepartmentTreeOptions
} from './department-options'
import type { DepartmentTreeNode } from './department-options'

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

function department(id: number, parentId: number | null, departmentName: string): Department {
  return {
    id,
    departmentNo: `DEP${id}`,
    organizationId: 1,
    parentId,
    departmentName,
    defaultDepartment: false,
    sortOrder: id,
    status: 'ACTIVE'
  }
}

function serializeTree(nodes: DepartmentTreeNode[]): unknown[] {
  return nodes.map((node) => ({
    departmentId: node.department.id,
    label: node.department.departmentName,
    depth: node.depth,
    children: serializeTree(node.children)
  }))
}

describe('buildDepartmentOptions', () => {
  it('builds a real department tree by parentId', () => {
    const tree = buildDepartmentTree([
      defaultDepartment,
      department(2, 1, '销售部'),
      department(3, 2, '销售一部'),
      department(4, 1, '技术部')
    ])

    expect(serializeTree(tree)).toEqual([
      {
        departmentId: 1,
        label: '默认部门',
        depth: 0,
        children: [
          {
            departmentId: 2,
            label: '销售部',
            depth: 1,
            children: [
              {
                departmentId: 3,
                label: '销售一部',
                depth: 2,
                children: []
              }
            ]
          },
          {
            departmentId: 4,
            label: '技术部',
            depth: 1,
            children: []
          }
        ]
      }
    ])
  })

  it('flattens department tree into UI options with depth', () => {
    const tree = buildDepartmentTree([
      defaultDepartment,
      department(2, 1, '销售部'),
      department(3, 2, '销售一部'),
      department(4, 1, '技术部')
    ])

    const options = flattenDepartmentTreeOptions(tree)

    expect(options.map((option) => ({
      departmentId: option.departmentId,
      label: option.label,
      depth: option.depth
    }))).toEqual([
      { departmentId: 1, label: '默认部门', depth: 0 },
      { departmentId: 2, label: '销售部', depth: 1 },
      { departmentId: 3, label: '销售一部', depth: 2 },
      { departmentId: 4, label: '技术部', depth: 1 }
    ])
  })

  it('keeps departments whose parent is missing as root nodes', () => {
    const orphanDepartment = department(5, 999, '异常部门')

    const tree = buildDepartmentTree([defaultDepartment, orphanDepartment])

    expect(serializeTree(tree)).toEqual([
      { departmentId: 1, label: '默认部门', depth: 0, children: [] },
      { departmentId: 5, label: '异常部门', depth: 0, children: [] }
    ])
  })

  it('keeps buildDepartmentOptions as a compatibility shortcut', () => {
    const options = buildDepartmentOptions([
      defaultDepartment,
      department(2, 1, '销售部')
    ])

    expect(options.map((option) => ({
      departmentId: option.departmentId,
      label: option.label,
      depth: option.depth
    }))).toEqual([
      { departmentId: 1, label: '默认部门', depth: 0 },
      { departmentId: 2, label: '销售部', depth: 1 }
    ])
  })
})
