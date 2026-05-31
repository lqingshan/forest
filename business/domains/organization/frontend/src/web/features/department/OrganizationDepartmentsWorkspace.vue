<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import type { Department } from '../../../shared/types'
import DepartmentSelect from '../../components/department/DepartmentSelect.vue'
import { createDepartment, deleteDepartment, listDepartments } from '../../api/organization-api'
import { buildDepartmentTree, flattenDepartmentTreeOptions } from '../../components/department/department-options'
import type { DepartmentOption } from '../../components/department/department-options'

const props = withDefaults(defineProps<{
  organizationNo?: string
}>(), {
  organizationNo: ''
})

const emit = defineEmits<{
  changed: []
}>()

const departments = ref<Department[]>([])
const departmentName = ref('')
const parentId = ref('')
const sortOrder = ref(0)
const submitting = ref(false)
const errorMessage = ref('')
const departmentTree = computed(() => buildDepartmentTree(departments.value))
const departmentRows = computed(() => {
  const departmentById = new Map(departments.value.map((department) => [department.id, department]))
  return flattenDepartmentTreeOptions(departmentTree.value)
    .map((option) => ({
      option,
      department: departmentById.get(option.departmentId)
    }))
    .filter((row): row is { option: DepartmentOption, department: Department } => Boolean(row.department))
})

onMounted(loadDepartments)
watch(() => props.organizationNo, loadDepartments)

async function loadDepartments() {
  departments.value = []
  parentId.value = ''
  if (!props.organizationNo) {
    return
  }
  errorMessage.value = ''
  try {
    departments.value = await listDepartments(props.organizationNo)
    ensureSelectedParentDepartment()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '部门列表加载失败'
  }
}

async function handleCreate() {
  if (!props.organizationNo || !departmentName.value) {
    errorMessage.value = '请填写部门名称'
    return
  }
  if (!parentId.value) {
    errorMessage.value = '请选择上级部门'
    return
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    await createDepartment(props.organizationNo, {
      parentId: Number(parentId.value),
      departmentName: departmentName.value,
      sortOrder: Number.isFinite(sortOrder.value) ? sortOrder.value : 0
    })
    departmentName.value = ''
    parentId.value = ''
    sortOrder.value = 0
    await loadDepartments()
    emit('changed')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '部门创建失败'
  } finally {
    submitting.value = false
  }
}

async function handleDelete(departmentId: number) {
  if (!props.organizationNo) {
    return
  }
  errorMessage.value = ''
  try {
    await deleteDepartment(props.organizationNo, departmentId)
    await loadDepartments()
    emit('changed')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '部门删除失败'
  }
}

function ensureSelectedParentDepartment() {
  // 部门列表刷新后，新增部门表单里的 parentId 可能已经不存在。
  // 这里保证前端始终选中一条真实部门记录：优先默认部门，兜底第一条部门。
  // 后端仍保留 parentId=null 的兜底能力，但当前页面不主动依赖这个隐式路径。
  const selectedDepartmentExists = departments.value.some((department) => String(department.id) === parentId.value)
  if (selectedDepartmentExists) {
    return
  }
  const defaultDepartment = departments.value.find((department) => department.defaultDepartment) ?? departments.value[0]
  parentId.value = defaultDepartment ? String(defaultDepartment.id) : ''
}
</script>

<template>
  <section class="workspace-page">
    <header class="workspace-page__header">
      <p class="workspace-page__eyebrow">Department</p>
      <h1 class="workspace-page__title">部门管理</h1>
      <p class="workspace-page__description">企业创建时会自动生成默认根部门，员工未指定部门时进入默认部门。</p>
    </header>

    <div v-if="!organizationNo" class="state">请先在企业入口页选择企业。</div>
    <div v-else class="workspace-grid workspace-grid--split">
      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>List</p>
            <h3>部门列表</h3>
          </div>

          <div v-if="errorMessage" class="state state--danger">{{ errorMessage }}</div>
          <div v-if="!departments.length" class="state">暂无部门。</div>
          <div v-else class="table-list">
            <div v-for="row in departmentRows" :key="row.department.id" class="table-list__row">
              <div class="department-list__main" :style="{ paddingLeft: `${row.option.depth * 18}px` }">
                <strong>{{ row.department.departmentName }}</strong>
                <span>{{ row.department.departmentNo }}</span>
              </div>
              <em>{{ row.department.defaultDepartment ? '默认部门' : '普通部门' }}</em>
              <button
                type="button"
                class="workspace-button-soft"
                :disabled="row.department.defaultDepartment"
                @click="handleDelete(row.department.id)"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Create</p>
            <h3>新增部门</h3>
          </div>
          <form class="stack" @submit.prevent="handleCreate">
            <label>
              上级部门
              <DepartmentSelect v-model="parentId" :departments="departments" />
            </label>
            <label>
              部门名称
              <input v-model.trim="departmentName" placeholder="请输入部门名称">
            </label>
            <label>
              排序
              <input v-model.number="sortOrder" type="number" min="0">
            </label>
            <button type="submit" class="workspace-button" :disabled="submitting">
              {{ submitting ? '保存中' : '新增部门' }}
            </button>
          </form>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.stack,
.table-list {
  display: grid;
  gap: 12px;
}

.stack label {
  display: grid;
  gap: 8px;
  color: var(--workspace-text-secondary);
}

.table-list__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 100px 90px;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-strong);
}

.department-list__main {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.table-list__row span,
.table-list__row em {
  color: var(--workspace-text-secondary);
  font-style: normal;
}

.state {
  padding: 16px;
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-soft);
  color: var(--workspace-text-secondary);
}

.state--danger {
  color: var(--workspace-danger);
  background: var(--workspace-danger-soft);
}
</style>
