<template>
  <OrganizationMembersWorkspace :organization-no="selectedOrganization?.organizationNo || ''" />
</template>

<script setup lang="ts">
import { OrganizationMembersWorkspace } from '@forest/organization/web'
import { selectedOrganization } from '../organization-state'
</script>
