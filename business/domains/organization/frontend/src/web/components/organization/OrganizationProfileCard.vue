<script setup lang="ts">
import type { Organization } from '../../../shared/types'
import { organizationCertificationStatusText, organizationStatusText } from '../../../shared/status-text'

defineProps<{
  organization: Organization
}>()
</script>

<template>
  <dl class="profile-list">
    <div>
      <dt>企业名称</dt>
      <dd>{{ organization.organizationName }}</dd>
    </div>
    <div>
      <dt>企业编号</dt>
      <dd>{{ organization.organizationNo }}</dd>
    </div>
    <div>
      <dt>企业状态</dt>
      <dd>{{ organizationStatusText(organization.status) }}</dd>
    </div>
    <div>
      <dt>认证状态</dt>
      <dd>{{ organizationCertificationStatusText(organization.certificationStatus) }}</dd>
    </div>
  </dl>
</template>

<style scoped>
.profile-list {
  display: grid;
  gap: 12px;
  margin: 0;
}

.profile-list div {
  display: grid;
  gap: 4px;
  padding: 14px;
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-strong);
}

.profile-list dt {
  color: var(--workspace-text-tertiary);
}

.profile-list dd {
  margin: 0;
  color: var(--workspace-text-primary);
}
</style>
