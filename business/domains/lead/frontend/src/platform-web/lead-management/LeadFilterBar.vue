<template>
  <form class="workspace-toolbar workspace-toolbar--lead" @submit.prevent="emit('submit')">
    <input :value="filters.keyword ?? ''" type="text" placeholder="按名称、类目或关键词搜索" @input="updateField('keyword', $event)">
    <input :value="filters.country ?? ''" type="text" placeholder="按国家查询" @input="updateField('country', $event)">
    <button type="submit" class="workspace-button">查询线索</button>
  </form>
</template>

<script setup lang="ts">
import type { LeadPlatformPageQuery } from './types'

const props = defineProps<{
  filters: LeadPlatformPageQuery
}>()

const emit = defineEmits<{
  update: [filters: LeadPlatformPageQuery]
  submit: []
}>()

function updateField(field: 'keyword' | 'country', event: Event) {
  const target = event.target as HTMLInputElement
  const value = target.value.trim()
  emit('update', {
    ...props.filters,
    [field]: value || undefined
  })
}
</script>
