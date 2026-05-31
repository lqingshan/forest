<template>
  <div class="workspace-table">
    <div class="workspace-table__head workspace-table__row users-grid">
      <span>ID</span>
      <span>名称</span>
      <span>手机号</span>
      <span>邮箱</span>
      <span>状态</span>
      <span>操作</span>
    </div>
    <button
      v-for="user in page.content"
      :key="user.id"
      type="button"
      class="workspace-table__row users-grid workspace-table__button"
      @click="emit('select', user.id)"
    >
      <span>{{ user.id }}</span>
      <span>{{ user.name || `用户 #${user.id}` }}</span>
      <span>{{ user.phone || '未设置' }}</span>
      <span>{{ user.email || '未设置' }}</span>
      <span>{{ user.status }}</span>
      <span>{{ selectedUserId === user.id ? '已选中' : '查看' }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import type { UserPage } from './types'

defineProps<{
  page: UserPage
  selectedUserId?: number | null
}>()

const emit = defineEmits<{
  select: [userId: number]
}>()
</script>

<style scoped>
.users-grid {
  grid-template-columns: 0.6fr 1.2fr 1fr 1.2fr 0.8fr 0.6fr;
}

@media (max-width: 1080px) {
  .users-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
