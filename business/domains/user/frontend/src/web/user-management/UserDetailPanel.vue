<template>
  <div class="user-detail-panel">
    <p v-if="!user" class="workspace-note">
      点击左侧用户行后，这里会展开当前用户的基础档案。你也可以从这里快速进入积分查询。
    </p>

    <UserCard v-else :user="user" />

    <UserStatusActions
      v-if="user"
      :user="user"
      :loading="actionLoading"
      @view-point="emit('view-point', $event)"
      @freeze="emit('freeze', $event)"
      @activate="emit('activate', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import UserCard from './UserCard.vue'
import UserStatusActions from './UserStatusActions.vue'
import type { UserManagementUser } from './types'

defineProps<{
  user: UserManagementUser | null
  actionLoading?: boolean
}>()

const emit = defineEmits<{
  'view-point': [userId: number]
  freeze: [userId: number]
  activate: [userId: number]
}>()
</script>

<style scoped>
.user-detail-panel {
  display: grid;
  gap: 18px;
}
</style>
