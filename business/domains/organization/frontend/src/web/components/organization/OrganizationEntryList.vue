<script setup lang="ts">
import type { Organization } from '../../../shared/types'
import { organizationCertificationStatusText } from '../../../shared/status-text'

withDefaults(defineProps<{
  organizations: Organization[]
  selectedOrganizationNo?: string
  loading?: boolean
  errorMessage?: string
}>(), {
  selectedOrganizationNo: '',
  loading: false,
  errorMessage: ''
})

const emit = defineEmits<{
  select: [organizationNo: string]
}>()
</script>

<template>
  <div v-if="errorMessage" class="state state--danger">{{ errorMessage }}</div>
  <div v-if="loading" class="state">加载中</div>
  <div v-else-if="!organizations.length" class="state">暂无企业，请先创建。</div>

  <div v-else class="organization-list">
    <button
      v-for="organization in organizations"
      :key="organization.organizationNo"
      type="button"
      class="organization-list__item"
      :class="{ 'is-active': organization.organizationNo === selectedOrganizationNo }"
      :aria-label="`进入企业 ${organization.organizationName}`"
      @click="emit('select', organization.organizationNo)"
    >
      <span class="organization-list__content">
        <strong>{{ organization.organizationName }}</strong>
        <span>{{ organization.organizationNo }}</span>
        <em>{{ organizationCertificationStatusText(organization.certificationStatus) }}</em>
      </span>
      <span class="organization-list__action">进入企业</span>
    </button>
  </div>
</template>

<style scoped>
.organization-list {
  display: grid;
  gap: 10px;
}

.organization-list__item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px;
  text-align: left;
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-strong);
  color: var(--workspace-text-primary);
  cursor: pointer;
}

.organization-list__item.is-active {
  border-color: var(--workspace-accent);
  background: var(--workspace-accent-soft);
}

.organization-list__content {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.organization-list__content > span,
.organization-list__content > em {
  color: var(--workspace-text-secondary);
  font-style: normal;
}

.organization-list__action {
  white-space: nowrap;
  padding: 8px 12px;
  border-radius: var(--workspace-radius-pill);
  background: var(--workspace-button-primary-background);
  color: var(--workspace-button-primary-text);
}

.state {
  padding: 14px;
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-soft);
  color: var(--workspace-text-secondary);
}

.state--danger {
  color: var(--workspace-danger);
  background: var(--workspace-danger-soft);
}
</style>
