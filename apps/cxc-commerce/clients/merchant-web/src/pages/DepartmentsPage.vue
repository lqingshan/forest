<template>
  <OrganizationDepartmentsWorkspace :organization-no="selectedOrganization?.organizationNo || ''" />
</template>

<script setup lang="ts">
import { OrganizationDepartmentsWorkspace } from '@forest/organization/web'
import { selectedOrganization } from '../organization-state'
</script>
