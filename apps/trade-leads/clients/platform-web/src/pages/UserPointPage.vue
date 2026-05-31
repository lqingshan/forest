<template>
  <UserPointQueryWorkspace
    :selected-user-id="selectedUserId"
    @select-user="selectUser"
    @selection-missing="clearSelectedUser"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserPointQueryWorkspace } from '@forest/user-point/platform-web/query'

const route = useRoute()
const router = useRouter()

const selectedUserId = computed(() => {
  const rawUserId = Array.isArray(route.query.userId) ? route.query.userId[0] : route.query.userId
  const routeUserId = Number(rawUserId)
  return Number.isFinite(routeUserId) && routeUserId > 0 ? routeUserId : undefined
})

async function selectUser(userId: number) {
  await router.push({
    path: '/user-point',
    query: { userId: String(userId) }
  })
}

async function clearSelectedUser() {
  await router.replace({ path: '/user-point' })
}
</script>
