<template>
  <OrganizationManagementWorkspace
    :organizations="organizationState.organizations"
    :selected-organization-no="organizationState.selectedOrganizationNo"
    :loading="organizationState.loading"
    :error-message="errorMessage"
    @select="selectOrganization"
    @created="handleCreated"
  />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { OrganizationManagementWorkspace } from '@forest/organization/web'
import type { Organization } from '@forest/organization/shared'
import { organizationState, refreshOrganizations, selectOrganization } from '../organization-state'

const errorMessage = ref('')

onMounted(() => loadOrganizations())

async function loadOrganizations(force = false) {
  errorMessage.value = ''
  try {
    await refreshOrganizations({ force })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '企业列表加载失败'
  }
}

async function handleCreated(organization: Organization) {
  await loadOrganizations(true)
  selectOrganization(organization.organizationNo)
}
</script>
