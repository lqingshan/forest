<template>
  <OrganizationAccessWorkspace
    :organization-no="selectedOrganization?.organizationNo || ''"
    :can="can"
  />
</template>

<script setup lang="ts">
import { OrganizationAccessWorkspace } from '@forest/organization-access/web'
import { can, selectedOrganization } from '../organization-state'
</script>
