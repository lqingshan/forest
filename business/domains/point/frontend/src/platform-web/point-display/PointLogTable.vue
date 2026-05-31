<template>
  <div class="point-log-table">
    <div class="point-log-table__head">
      <span>时间</span>
      <span>出入</span>
      <span>金额</span>
      <span>余额</span>
      <span>来源</span>
      <span>业务键</span>
    </div>
    <div v-for="log in logs" :key="log.id" class="point-log-table__row">
      <span>{{ formatToMinute(log.createdTime) }}</span>
      <span>{{ log.direction }}</span>
      <span>{{ log.amount }}</span>
      <span>{{ log.balanceAfter }}</span>
      <span>{{ log.sourceType }}</span>
      <span>{{ log.bizKey }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { PointLogItem } from './types'

defineProps<{ logs: PointLogItem[] }>()

function formatToMinute(value: string) {
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
.point-log-table {
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-lg);
  overflow: hidden;
  background: var(--workspace-surface-strong);
}

.point-log-table__head,
.point-log-table__row {
  display: grid;
  grid-template-columns: 1.4fr 0.8fr 0.8fr 0.8fr 0.8fr 1.4fr;
  gap: 12px;
  padding: 14px 16px;
}

.point-log-table__head {
  background: var(--workspace-table-header-bg);
  color: var(--workspace-table-header-text);
  font-family: var(--workspace-font-mono);
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.point-log-table__row {
  color: var(--workspace-text-primary);
  border-top: 1px solid var(--workspace-border-soft);
}
</style>
