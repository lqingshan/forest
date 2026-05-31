<template>
  <div class="current-user-badge" v-if="session.state.currentUser">
    <button class="current-user-badge__avatar" type="button" :disabled="uploading" @click="fileInput?.click()">
      <img
        v-if="session.state.currentUser.avatarUrl"
        :src="session.state.currentUser.avatarUrl"
        alt=""
      >
      <span v-else>{{ principalInitial }}</span>
    </button>
    <input
      ref="fileInput"
      type="file"
      accept="image/jpeg,image/png,image/webp,image/gif"
      hidden
      :disabled="uploading"
      @change="handleAvatarChange"
    >
    <div class="current-user-badge__body">
      <span>当前登录</span>
      <strong>{{ displayName }}</strong>
      <small>{{ uploading ? '头像上传中' : '统一用户体系下的认证主体' }}</small>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { WebUserSession } from '../auth/session-factory'

const props = defineProps<{
  session: WebUserSession
}>()

const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

const displayName = computed(() => {
  const user = props.session.state.currentUser
  return user?.loginName || user?.name || user?.phone || '当前用户'
})

const principalInitial = computed(() => displayName.value.slice(0, 1))

async function handleAvatarChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  target.value = ''
  if (!file || uploading.value) {
    return
  }
  uploading.value = true
  try {
    await props.session.uploadAvatar(file)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.current-user-badge {
  padding: 16px;
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-lg);
  background: var(--workspace-surface);
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-user-badge__avatar {
  width: 48px;
  height: 48px;
  border: 0;
  border-radius: 999px;
  padding: 0;
  overflow: hidden;
  background: var(--workspace-surface-muted, #eef2f7);
  color: var(--workspace-text-primary, #1f2937);
  display: grid;
  place-items: center;
  cursor: pointer;
  flex: none;
}

.current-user-badge__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.current-user-badge__avatar span {
  font-size: 18px;
  font-weight: 700;
}

.current-user-badge__body {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.current-user-badge__body > span {
  font-size: 11px;
  font-family: var(--workspace-font-mono);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--workspace-text-tertiary);
}

.current-user-badge strong {
  font-size: 22px;
}

.current-user-badge small {
  color: var(--workspace-text-secondary);
  line-height: 1.5;
}
</style>
