<template>
  <OrganizationCertificationWorkspace :organization-no="selectedOrganization?.organizationNo || ''" />
</template>

<script setup lang="ts">
import { OrganizationCertificationWorkspace } from '@forest/organization/web'
import { selectedOrganization } from '../organization-state'
</script>
