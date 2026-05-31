<template>
  <div class="user-card">
    <div class="user-card__header">
      <div>
        <h3>{{ user.name || `用户 #${user.id}` }}</h3>
        <p>ID: {{ user.id }}</p>
      </div>
      <span class="user-card__status">{{ user.status }}</span>
    </div>
    <div class="user-card__body">
      <p>头像：{{ user.avatarUrl || user.avatar || '未设置' }}</p>
      <p>手机号：{{ user.phone || '未设置' }}</p>
      <p>邮箱：{{ user.email || '未设置' }}</p>
      <p>创建时间：{{ user.createdTime || '未知' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { UserManagementUser } from './types'

defineProps<{ user: UserManagementUser }>()
</script>

<style scoped>
.user-card {
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-lg);
  padding: 18px;
  background: linear-gradient(180deg, var(--workspace-card-paper-start), var(--workspace-card-paper-end));
}

.user-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.user-card__header h3 {
  margin: 0;
  font-size: 24px;
  color: var(--workspace-text-primary);
  font-family: var(--workspace-font-heading);
}

.user-card__header p,
.user-card__body p {
  margin: 4px 0 0;
  color: var(--workspace-text-secondary);
  line-height: 1.7;
}

.user-card__status {
  padding: 6px 10px;
  border-radius: var(--workspace-radius-pill);
  background: var(--workspace-accent-soft);
  color: var(--workspace-accent-strong);
  font-size: 11px;
  font-family: var(--workspace-font-mono);
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
</style>
