<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import type { Department, Member } from '../../../shared/types'
import { memberStatusText } from '../../../shared/status-text'
import DepartmentSelect from '../../components/department/DepartmentSelect.vue'
import {
  activateMember,
  addMember,
  disableMember,
  listDepartments,
  listMembers
} from '../../api/organization-api'

const props = withDefaults(defineProps<{
  organizationNo?: string
}>(), {
  organizationNo: ''
})

const emit = defineEmits<{
  changed: []
}>()

const members = ref<Member[]>([])
const departments = ref<Department[]>([])
const submitting = ref(false)
const errorMessage = ref('')
const form = reactive({
  phone: '',
  name: '',
  initialPassword: '',
  departmentId: ''
})

onMounted(load)
watch(() => props.organizationNo, load)

async function load() {
  members.value = []
  departments.value = []
  form.departmentId = ''
  if (!props.organizationNo) {
    return
  }
  errorMessage.value = ''
  try {
    const [memberList, departmentList] = await Promise.all([
      listMembers(props.organizationNo),
      listDepartments(props.organizationNo)
    ])
    members.value = memberList
    departments.value = departmentList
    ensureSelectedDepartment()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '员工数据加载失败'
  }
}

async function handleAdd() {
  if (!props.organizationNo || !form.phone || !form.initialPassword) {
    errorMessage.value = '请填写手机号和初始密码'
    return
  }
  if (!form.departmentId) {
    errorMessage.value = '请选择部门'
    return
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    await addMember(props.organizationNo, {
      phone: form.phone,
      name: form.name,
      initialPassword: form.initialPassword,
      departmentId: Number(form.departmentId)
    })
    form.phone = ''
    form.name = ''
    form.initialPassword = ''
    form.departmentId = ''
    await load()
    emit('changed')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '员工添加失败'
  } finally {
    submitting.value = false
  }
}

async function handleDisable(memberId: number) {
  if (!props.organizationNo) {
    return
  }
  try {
    await disableMember(props.organizationNo, memberId)
    await load()
    emit('changed')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '员工停用失败'
  }
}

async function handleActivate(memberId: number) {
  if (!props.organizationNo) {
    return
  }
  try {
    await activateMember(props.organizationNo, memberId)
    await load()
    emit('changed')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '员工启用失败'
  }
}

function ensureSelectedDepartment() {
  const selectedDepartmentExists = departments.value.some((department) => String(department.id) === form.departmentId)
  if (selectedDepartmentExists) {
    return
  }
  const defaultDepartment = departments.value.find((department) => department.defaultDepartment) ?? departments.value[0]
  form.departmentId = defaultDepartment ? String(defaultDepartment.id) : ''
}
</script>

<template>
  <section class="workspace-page">
    <header class="workspace-page__header">
      <p class="workspace-page__eyebrow">Member</p>
      <h1 class="workspace-page__title">员工管理</h1>
      <p class="workspace-page__description">员工绑定自然人 user。手机号已有账号时复用，未注册手机号会预创建 user、手机号账号和密码账号。</p>
    </header>

    <div v-if="!organizationNo" class="state">请先在企业入口页选择企业。</div>
    <div v-else class="workspace-grid workspace-grid--split">
      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Employees</p>
            <h3>企业员工</h3>
          </div>

          <div v-if="errorMessage" class="state state--danger">{{ errorMessage }}</div>
          <div v-if="!members.length" class="state">暂无员工。</div>
          <div v-else class="member-list">
            <div v-for="member in members" :key="member.memberId" class="member-list__row">
              <div>
                <strong>{{ member.name || '未命名员工' }}</strong>
                <span>{{ member.phone || '-' }}</span>
              </div>
              <span>{{ memberStatusText(member.status) }}</span>
              <button
                type="button"
                class="workspace-button-soft"
                @click="member.status === 'ACTIVE' ? handleDisable(member.memberId) : handleActivate(member.memberId)"
              >
                {{ member.status === 'ACTIVE' ? '停用' : '启用' }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Add</p>
            <h3>添加员工</h3>
          </div>
          <form class="stack" @submit.prevent="handleAdd">
            <label>
              手机号
              <input v-model.trim="form.phone" placeholder="请输入员工手机号">
            </label>
            <label>
              姓名
              <input v-model.trim="form.name" placeholder="请输入员工姓名">
            </label>
            <label>
              初始密码
              <input v-model="form.initialPassword" type="password" placeholder="管理员直接设置密码">
            </label>
            <label>
              部门
              <DepartmentSelect v-model="form.departmentId" :departments="departments" />
            </label>
            <button type="submit" class="workspace-button" :disabled="submitting">
              {{ submitting ? '添加中' : '添加员工' }}
            </button>
          </form>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.stack,
.member-list {
  display: grid;
  gap: 12px;
}

.stack label {
  display: grid;
  gap: 8px;
  color: var(--workspace-text-secondary);
}

.member-list__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 80px 90px;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-strong);
}

.member-list__row div {
  display: grid;
  gap: 4px;
}

.member-list__row span {
  color: var(--workspace-text-secondary);
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
