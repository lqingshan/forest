<template>
  <UserManagementWorkspace @view-point="goToPoint" />
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { UserManagementWorkspace } from '@forest/user/web/user-management'

const router = useRouter()

async function goToPoint(userId: number) {
  await router.push(`/user-point?userId=${userId}`)
}
</script>
