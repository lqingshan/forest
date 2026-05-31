<template>
  <form class="workspace-toolbar workspace-toolbar--users" @submit.prevent="emit('submit')">
    <input :value="filters.id" type="number" placeholder="按用户 ID 查询" @input="updateNumber('id', $event)">
    <input :value="filters.name ?? ''" type="text" placeholder="按名称查询" @input="updateText('name', $event)">
    <input :value="filters.phone ?? ''" type="text" placeholder="按手机号查询" @input="updateText('phone', $event)">
    <input :value="filters.email ?? ''" type="text" placeholder="按邮箱查询" @input="updateText('email', $event)">
    <select :value="filters.status ?? ''" @change="updateStatus">
      <option value="">全部状态</option>
      <option value="ACTIVE">ACTIVE</option>
      <option value="FROZEN">FROZEN</option>
      <option value="DISABLED">DISABLED</option>
    </select>
    <button type="submit" class="workspace-button">查询</button>
  </form>
</template>

<script setup lang="ts">
import type { UserPageQuery, UserStatus } from './types'

const props = defineProps<{
  filters: UserPageQuery
}>()

const emit = defineEmits<{
  update: [filters: UserPageQuery]
  submit: []
}>()

function updateNumber(field: 'id', event: Event) {
  const target = event.target as HTMLInputElement
  const value = target.value.trim()
  emit('update', {
    ...props.filters,
    [field]: value ? Number(value) : undefined
  })
}

function updateText(field: 'name' | 'phone' | 'email', event: Event) {
  const target = event.target as HTMLInputElement | HTMLSelectElement
  const value = target.value.trim()
  emit('update', {
    ...props.filters,
    [field]: value || undefined
  })
}

function updateStatus(event: Event) {
  const target = event.target as HTMLSelectElement
  const value = target.value.trim()
  emit('update', {
    ...props.filters,
    status: value ? value as UserStatus : undefined
  })
}
</script>
