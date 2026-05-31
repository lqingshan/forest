<script setup lang="ts">
import { ref } from 'vue'
import type { User as CurrentUser } from '../../../shared/user'
import type { WebUserSession } from '../../../web/auth'
import { loginWithNativeCarrier } from '../session'
import type { NativeCarrierAuthBridge } from '../bridge'

const props = withDefaults(defineProps<{
  session: WebUserSession
  bridge?: NativeCarrierAuthBridge
  title?: string
  description?: string
  submitText?: string
  provider?: string
}>(), {
  title: '本机号一键登录',
  description: '通过 APP 原生能力获取本机号授权凭证完成登录。',
  submitText: '本机号一键登录',
  provider: undefined
})

const emit = defineEmits<{
  success: [user: CurrentUser]
}>()

const submitting = ref(false)
const errorMessage = ref('')

async function handleSubmit() {
  submitting.value = true
  errorMessage.value = ''
  try {
    const user = await loginWithNativeCarrier(props.session, { provider: props.provider }, props.bridge)
    emit('success', user)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '本机号一键登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="forest-carrier-login-panel">
    <div class="forest-carrier-login-panel__heading">
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </div>
    <p v-if="errorMessage" class="forest-carrier-login-panel__error">{{ errorMessage }}</p>
    <button type="button" class="workspace-button" :disabled="submitting" @click="handleSubmit">
      {{ submitting ? '登录中' : submitText }}
    </button>
  </section>
</template>

<style scoped>
.forest-carrier-login-panel {
  display: grid;
  gap: 14px;
  width: 100%;
}

.forest-carrier-login-panel__heading {
  display: grid;
  gap: 6px;
}

.forest-carrier-login-panel__heading h2 {
  margin: 0;
  font-size: 20px;
}

.forest-carrier-login-panel__heading p,
.forest-carrier-login-panel__error {
  margin: 0;
}

.forest-carrier-login-panel__error {
  color: var(--forest-login-error-color, #b42318);
}
</style>
