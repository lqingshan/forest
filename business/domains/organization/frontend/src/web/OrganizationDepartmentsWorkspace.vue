<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import type { Department } from '../shared/types'
import { createDepartment, deleteDepartment, listDepartments } from './api'

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

onMounted(loadDepartments)
watch(() => props.organizationNo, loadDepartments)

async function loadDepartments() {
  departments.value = []
  if (!props.organizationNo) {
    return
  }
  errorMessage.value = ''
  try {
    departments.value = await listDepartments(props.organizationNo)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '部门列表加载失败'
  }
}

async function handleCreate() {
  if (!props.organizationNo || !departmentName.value) {
    errorMessage.value = '请填写部门名称'
    return
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    await createDepartment(props.organizationNo, {
      parentId: parentId.value ? Number(parentId.value) : null,
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
</script>

<template>
  <section class="workspace-page">
    <header class="workspace-page__header">
      <p class="workspace-page__eyebrow">Department</p>
      <h1 class="workspace-page__title">部门管理</h1>
      <p class="workspace-page__description">企业创建时会自动生成默认根部门，员工未指定部门时进入默认部门。</p>
    </header>

    <div v-if="!organizationNo" class="state">请先在企业页选择或创建企业。</div>
    <div v-else class="workspace-grid workspace-grid--split">
      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Tree</p>
            <h3>部门列表</h3>
          </div>

          <div v-if="errorMessage" class="state state--danger">{{ errorMessage }}</div>
          <div v-if="!departments.length" class="state">暂无部门。</div>
          <div v-else class="table-list">
            <div v-for="department in departments" :key="department.id" class="table-list__row">
              <div>
                <strong>{{ department.departmentName }}</strong>
                <span>{{ department.departmentNo }}</span>
              </div>
              <em>{{ department.defaultDepartment ? '默认部门' : '普通部门' }}</em>
              <button
                type="button"
                class="workspace-button-soft"
                :disabled="department.defaultDepartment"
                @click="handleDelete(department.id)"
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
              <select v-model="parentId">
                <option value="">默认根部门</option>
                <option v-for="department in departments" :key="department.id" :value="String(department.id)">
                  {{ department.departmentName }}
                </option>
              </select>
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

.table-list__row div {
  display: grid;
  gap: 4px;
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
