<template>
  <div class="workspace-grid workspace-grid--split">
    <section class="workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <div class="workspace-card__heading">
          <p>User Registry</p>
          <h3>用户列表</h3>
        </div>

        <UserFilterBar :filters="filters" @update="filters = $event" @submit="loadUsers(0)" />

        <p v-if="errorMessage" class="workspace-error">{{ errorMessage }}</p>

        <UserTable
          :page="userPage"
          :selected-user-id="selectedUser?.id ?? null"
          @select="selectUser"
        />

        <div class="workspace-pager">
          <span>第 {{ userPage.number + 1 }} / {{ Math.max(userPage.totalPages, 1) }} 页</span>
          <div class="workspace-pager__actions">
            <button type="button" class="workspace-button-soft" :disabled="userPage.number <= 0 || loading" @click="loadUsers(userPage.number - 1)">
              上一页
            </button>
            <button
              type="button"
              class="workspace-button-soft"
              :disabled="userPage.number + 1 >= userPage.totalPages || loading"
              @click="loadUsers(userPage.number + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <div class="workspace-card__heading">
          <p>User Dossier</p>
          <h3>用户档案</h3>
        </div>

        <UserDetailPanel
          :user="selectedUser"
          :action-loading="actionLoading"
          @view-point="emit('view-point', $event)"
          @freeze="updateStatus($event, 'freeze')"
          @activate="updateStatus($event, 'activate')"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { activateUser, fetchUser, fetchUsers, freezeUser } from './api'
import type { UserManagementUser, UserPage, UserPageQuery } from './types'
import UserDetailPanel from './UserDetailPanel.vue'
import UserFilterBar from './UserFilterBar.vue'
import UserTable from './UserTable.vue'

const emit = defineEmits<{
  'view-point': [userId: number]
}>()

const filters = ref<UserPageQuery>({
  id: undefined,
  name: undefined,
  phone: undefined,
  email: undefined,
  status: undefined
})

const userPage = ref<UserPage>({
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 20,
  number: 0
})

const selectedUser = ref<UserManagementUser | null>(null)
const loading = ref(false)
const actionLoading = ref(false)
const errorMessage = ref('')

onMounted(async () => {
  await loadUsers(0)
})

async function loadUsers(page: number) {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await fetchUsers({
      page,
      size: userPage.value.size,
      id: filters.value.id,
      name: filters.value.name,
      phone: filters.value.phone,
      email: filters.value.email,
      status: filters.value.status
    })
    userPage.value = result
    if (selectedUser.value) {
      const refreshed = result.content.find((item) => item.id === selectedUser.value?.id)
      if (refreshed) {
        selectedUser.value = refreshed
      }
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户列表加载失败'
  } finally {
    loading.value = false
  }
}

async function selectUser(userId: number) {
  try {
    selectedUser.value = await fetchUser(userId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户详情加载失败'
  }
}

async function updateStatus(userId: number, action: 'freeze' | 'activate') {
  actionLoading.value = true
  errorMessage.value = ''
  try {
    selectedUser.value = action === 'freeze'
      ? await freezeUser(userId)
      : await activateUser(userId)
    await loadUsers(userPage.value.number)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户状态更新失败'
  } finally {
    actionLoading.value = false
  }
}
</script>
