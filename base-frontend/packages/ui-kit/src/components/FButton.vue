<template>
  <button class="f-button" :class="[`f-button--${type}`, { 'is-loading': loading }]" :disabled="loading">
    <span v-if="loading" class="f-button-loading">加载中...</span>
    <slot></slot>
  </button>
</template>

<script setup lang="ts">
defineProps<{ type?: 'primary' | 'default' | 'danger'; loading?: boolean }>()
</script>

<style scoped>
.f-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  font-size: 14px;
  border-radius: var(--workspace-radius-md, 14px);
  border: 1px solid transparent;
  cursor: pointer;
  transition:
    transform var(--workspace-motion-fast, 160ms) var(--workspace-ease-standard, ease),
    box-shadow var(--workspace-motion-fast, 160ms) var(--workspace-ease-standard, ease),
    background-color var(--workspace-motion-fast, 160ms) var(--workspace-ease-standard, ease),
    border-color var(--workspace-motion-fast, 160ms) var(--workspace-ease-standard, ease);
}

.f-button--default {
  background: var(--workspace-surface-strong, #fff);
  border-color: var(--workspace-border-soft, #dcdcdc);
  color: var(--workspace-text-secondary, #606266);
}

.f-button--primary {
  background: var(--workspace-button-primary-background, #4a90d9);
  border-color: transparent;
  color: var(--workspace-button-primary-text, #fff);
  box-shadow: var(--workspace-button-primary-shadow, 0 10px 24px rgba(35, 32, 28, 0.12));
}

.f-button--danger {
  background: var(--workspace-danger, #f56c6c);
  border-color: transparent;
  color: var(--workspace-text-inverse, #fff);
}
</style>
