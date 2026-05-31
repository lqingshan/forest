<template>
  <div class="workspace-table">
    <div class="workspace-table__head workspace-table__row lead-grid">
      <span>名称</span>
      <span>国家</span>
      <span>分类</span>
      <span>邮箱</span>
      <span>操作</span>
    </div>
    <button
      v-for="lead in page.content"
      :key="lead.id"
      type="button"
      class="workspace-table__row lead-grid workspace-table__button"
      @click="emit('select', lead.id)"
    >
      <span>{{ lead.name }}</span>
      <span>{{ lead.country || '未知' }}</span>
      <span>{{ lead.category || '未分类' }}</span>
      <span>{{ lead.email || '未填写' }}</span>
      <span>{{ selectedLeadId === lead.id ? '已选中' : '查看' }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import type { LeadPlatformPage } from './types'

defineProps<{
  page: LeadPlatformPage
  selectedLeadId?: number | null
}>()

const emit = defineEmits<{
  select: [leadId: number]
}>()
</script>

<style scoped>
.lead-grid {
  grid-template-columns: 1.4fr 0.7fr 0.9fr 1.2fr 0.5fr;
}

@media (max-width: 1080px) {
  .lead-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
