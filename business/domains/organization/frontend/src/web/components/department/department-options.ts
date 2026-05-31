import type { Department } from '../../../shared/types'

export interface DepartmentOption {
  value: string
  label: string
  depth: number
  departmentId: number
  defaultDepartment: boolean
}

export interface DepartmentTreeNode {
  department: Department
  children: DepartmentTreeNode[]
  depth: number
}

/**
 * 把后端返回的平铺部门列表转换成真实树状结构。
 *
 * <p>这里不重新按 sortOrder 排序，而是保留后端返回顺序；只根据 parentId 建立
 * children 关系。parentId 缺失、指向自身或异常循环的数据会作为根级展示，避免 UI 丢部门。</p>
 */
export function buildDepartmentTree(departments: Department[]): DepartmentTreeNode[] {
  const departmentById = new Map(departments.map((department) => [department.id, department]))
  const childrenByParentId = new Map<number, Department[]>()
  const roots: Department[] = []

  for (const department of departments) {
    if (
      department.parentId !== null
      && department.parentId !== department.id
      && departmentById.has(department.parentId)
    ) {
      const children = childrenByParentId.get(department.parentId) ?? []
      children.push(department)
      childrenByParentId.set(department.parentId, children)
    } else {
      roots.push(department)
    }
  }

  const tree: DepartmentTreeNode[] = []
  const visitedDepartmentIds = new Set<number>()

  const buildNode = (department: Department, depth: number): DepartmentTreeNode | null => {
    if (visitedDepartmentIds.has(department.id)) {
      return null
    }
    visitedDepartmentIds.add(department.id)
    const node: DepartmentTreeNode = {
      department,
      children: [],
      depth
    }

    for (const child of childrenByParentId.get(department.id) ?? []) {
      const childNode = buildNode(child, depth + 1)
      if (childNode) {
        node.children.push(childNode)
      }
    }

    return node
  }

  const appendRoot = (department: Department) => {
    const node = buildNode(department, 0)
    if (node) {
      tree.push(node)
    }
  }

  for (const root of roots) {
    appendRoot(root)
  }

  for (const department of departments) {
    appendRoot(department)
  }

  return tree
}

/**
 * 把真实部门树转换成 select/list 这类扁平 UI 需要的层级选项。
 */
export function flattenDepartmentTreeOptions(tree: DepartmentTreeNode[]): DepartmentOption[] {
  const options: DepartmentOption[] = []

  const appendNode = (node: DepartmentTreeNode) => {
    options.push({
      value: String(node.department.id),
      label: node.department.departmentName,
      depth: node.depth,
      departmentId: node.department.id,
      defaultDepartment: node.department.defaultDepartment
    })

    for (const child of node.children) {
      appendNode(child)
    }
  }

  for (const node of tree) {
    appendNode(node)
  }

  return options
}

/**
 * 把部门列表转换成下拉和列表展示选项。
 *
 * <p>这是兼容入口；内部先构建真实树，再为当前 UI 做扁平化展示。</p>
 */
export function buildDepartmentOptions(departments: Department[]): DepartmentOption[] {
  return flattenDepartmentTreeOptions(buildDepartmentTree(departments))
}
