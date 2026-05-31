<template>
  <div class="workspace-grid workspace-grid--split user-point-layout">
    <section class="workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <div class="workspace-card__heading">
          <p>User Points Lookup</p>
          <h3>查找用户积分</h3>
        </div>

        <form class="workspace-toolbar user-point-search__form" @submit.prevent="searchRows">
          <input v-model.trim="filters.name" type="text" placeholder="用户名">
          <input v-model.trim="filters.phone" type="text" placeholder="手机号，至少 2 个字符">
          <input v-model.trim="filters.email" type="text" placeholder="邮箱，至少 2 个字符">
          <button type="submit" class="workspace-button" :disabled="loading">
            {{ loading ? '查询中' : '查询用户积分' }}
          </button>
        </form>

        <p v-if="errorMessage" class="workspace-error">{{ errorMessage }}</p>

        <div v-if="rows.length" class="workspace-table user-point-search__table">
          <div class="workspace-table__head workspace-table__row user-point-grid">
            <span>ID</span>
            <span>用户名</span>
            <span>手机号</span>
            <span>邮箱</span>
            <span>余额</span>
            <span>更新时间</span>
            <span>状态</span>
          </div>
          <button
            v-for="row in rows"
            :key="row.user.id"
            type="button"
            class="workspace-table__row workspace-table__button user-point-grid"
            :class="{ 'is-selected': selectedUserId === row.user.id }"
            @click="selectRow(row)"
          >
            <span>{{ row.user.id }}</span>
            <span>{{ row.user.name || `用户 #${row.user.id}` }}</span>
            <span>{{ row.user.phone || '未设置' }}</span>
            <span>{{ row.user.email || '未设置' }}</span>
            <span>{{ row.points.balance }}</span>
            <span>{{ formatUpdatedAt(row.points.updatedAt) }}</span>
            <span>{{ row.user.status }}</span>
          </button>
        </div>

        <p v-else-if="searched" class="workspace-note">没有找到匹配的用户积分记录。</p>
        <p v-else class="workspace-note">可直接空条件查询全部积分用户；手机号或邮箱请输入至少 2 个字符。</p>

        <div v-if="searched && pageResult.totalElements > 0" class="workspace-pager">
          <span>共 {{ pageResult.totalElements }} 条，第 {{ pageResult.number + 1 }} / {{ Math.max(pageResult.totalPages, 1) }} 页</span>
          <div class="workspace-pager__actions">
            <button
              type="button"
              class="workspace-button-soft"
              :disabled="pageResult.number <= 0 || loading"
              @click="loadPage(pageResult.number - 1)"
            >
              上一页
            </button>
            <button
              type="button"
              class="workspace-button-soft"
              :disabled="pageResult.number + 1 >= pageResult.totalPages || loading"
              @click="loadPage(pageResult.number + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </section>

    <UserPointWorkspace :user-id="selectedUserId" />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { fetchUserPointPage } from './api'
import type { UserPointPage, UserPointRow } from './types'
import UserPointWorkspace from './UserPointWorkspace.vue'

const DEFAULT_PAGE_SIZE = 20

const props = defineProps<{
  selectedUserId?: number
}>()

const emit = defineEmits<{
  'select-user': [userId: number]
  'selection-missing': []
}>()

const filters = reactive({
  name: '',
  phone: '',
  email: ''
})
const pageResult = ref<UserPointPage>(createEmptyPage())
const loading = ref(false)
const errorMessage = ref('')
const searched = ref(false)
const rows = computed(() => pageResult.value.content)
const selectedUserId = computed(() => props.selectedUserId)

async function searchRows() {
  const validationMessage = validateFilters()
  if (validationMessage) {
    errorMessage.value = validationMessage
    return
  }
  await loadPage(0)
}

async function loadPage(page: number) {
  const name = filters.name.trim()
  const phone = filters.phone.trim()
  const email = filters.email.trim()

  loading.value = true
  errorMessage.value = ''
  try {
    const result = await fetchUserPointPage({
      page,
      size: pageResult.value.size || DEFAULT_PAGE_SIZE,
      name: name || undefined,
      phone: phone || undefined,
      email: email || undefined
    })
    pageResult.value = result
    searched.value = true
    syncSelection(result.content)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户积分查询失败'
    pageResult.value = createEmptyPage()
    searched.value = false
  } finally {
    loading.value = false
  }
}

function selectRow(row: UserPointRow) {
  emit('select-user', row.user.id)
}

function validateFilters() {
  const phone = filters.phone.trim()
  const email = filters.email.trim()
  if ((phone && phone.length < 2) || (email && email.length < 2)) {
    return '手机号或邮箱至少输入 2 个字符'
  }
  return ''
}

function createEmptyPage(): UserPointPage {
  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: DEFAULT_PAGE_SIZE,
    number: 0
  }
}

function syncSelection(content: UserPointRow[]) {
  if (!selectedUserId.value) {
    return
  }
  if (content.some((row) => row.user.id === selectedUserId.value)) {
    return
  }
  emit('selection-missing')
}

function formatUpdatedAt(value: string | null) {
  if (!value) {
    return '未更新'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value.replace('T', ' ').slice(0, 16)
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}
</script>

<style scoped>
.user-point-layout {
  align-items: start;
}

.user-point-search__form {
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
}

.user-point-search__form .workspace-button {
  min-width: 160px;
}

.user-point-search__table {
  margin-top: 16px;
}

.user-point-grid {
  grid-template-columns: 72px minmax(120px, 1fr) minmax(130px, 1fr) minmax(180px, 1.3fr) 92px minmax(180px, 1.1fr) 96px;
  align-items: center;
}

.workspace-table__button.is-selected {
  background: var(--workspace-table-row-hover);
  box-shadow: inset 3px 0 0 var(--workspace-button-primary-background);
}

@media (max-width: 1080px) {
  .user-point-search__form,
  .user-point-grid {
    grid-template-columns: 1fr;
  }
}
</style>
