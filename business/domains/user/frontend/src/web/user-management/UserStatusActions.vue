<template>
  <div class="workspace-actions" v-if="user">
    <button type="button" class="workspace-button" @click="emit('view-point', user.id)">
      查看积分
    </button>
    <button
      v-if="user.status !== 'FROZEN'"
      type="button"
      class="workspace-button-danger"
      :disabled="loading || user.name === 'admin'"
      @click="emit('freeze', user.id)"
    >
      冻结用户
    </button>
    <button
      v-else
      type="button"
      class="workspace-button-success"
      :disabled="loading"
      @click="emit('activate', user.id)"
    >
      恢复用户
    </button>
    <span v-if="user.name === 'admin' && user.status !== 'FROZEN'" class="workspace-note">admin 用户不能被冻结。</span>
  </div>
</template>

<script setup lang="ts">
import type { UserManagementUser } from './types'

defineProps<{
  user: UserManagementUser | null
  loading?: boolean
}>()

const emit = defineEmits<{
  'view-point': [userId: number]
  freeze: [userId: number]
  activate: [userId: number]
}>()
</script>
