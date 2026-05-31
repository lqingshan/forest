<script setup lang="ts">
import { ref } from 'vue'
import type { User as CurrentUser } from '../../../shared/user'
import PhonePasswordLoginPanel from '../components/PhonePasswordLoginPanel.vue'
import PhoneSmsLoginPanel from '../components/PhoneSmsLoginPanel.vue'
import type { WebUserSession } from '../session-factory'

type LoginMode = 'password' | 'sms'

const props = withDefaults(defineProps<{
  session: WebUserSession
  defaultMode?: LoginMode
  passwordTitle?: string
  passwordSubmitText?: string
  smsTitle?: string
  smsSubmitText?: string
}>(), {
  defaultMode: 'password',
  passwordTitle: '手机号密码登录',
  passwordSubmitText: '密码登录',
  smsTitle: '手机验证码登录',
  smsSubmitText: '验证码登录'
})

const emit = defineEmits<{
  success: [user: CurrentUser]
}>()

const activeMode = ref<LoginMode>(props.defaultMode)

function handleSuccess(user: CurrentUser) {
  emit('success', user)
}
</script>

<template>
  <div class="forest-web-pc-login-panel">
    <div class="forest-web-pc-login-panel__tabs" role="tablist" aria-label="登录方式">
      <button
        type="button"
        role="tab"
        class="forest-web-pc-login-panel__tab"
        :class="{ 'forest-web-pc-login-panel__tab--active': activeMode === 'password' }"
        :aria-selected="activeMode === 'password'"
        @click="activeMode = 'password'"
      >
        账号密码
      </button>
      <button
        type="button"
        role="tab"
        class="forest-web-pc-login-panel__tab"
        :class="{ 'forest-web-pc-login-panel__tab--active': activeMode === 'sms' }"
        :aria-selected="activeMode === 'sms'"
        @click="activeMode = 'sms'"
      >
        手机验证码
      </button>
    </div>

    <PhonePasswordLoginPanel
      v-if="activeMode === 'password'"
      :session="session"
      :title="passwordTitle"
      :submit-text="passwordSubmitText"
      @success="handleSuccess"
    />
    <PhoneSmsLoginPanel
      v-else
      :session="session"
      :title="smsTitle"
      :submit-text="smsSubmitText"
      @success="handleSuccess"
    />
  </div>
</template>

<style scoped>
.forest-web-pc-login-panel {
  display: grid;
  gap: var(--forest-web-pc-login-gap, 16px);
  width: 100%;
}

.forest-web-pc-login-panel__tabs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--forest-web-pc-login-tab-gap, 4px);
  padding: var(--forest-web-pc-login-tabs-padding, 4px);
  border: var(--forest-web-pc-login-tabs-border, 1px solid #d8dee8);
  border-radius: var(--forest-web-pc-login-tabs-radius, 999px);
  background: var(--forest-web-pc-login-tabs-background, #f4f6f8);
}

.forest-web-pc-login-panel__tab {
  min-width: 0;
  border: 0;
  border-radius: var(--forest-web-pc-login-tab-radius, 999px);
  padding: var(--forest-web-pc-login-tab-padding, 10px 12px);
  background: transparent;
  color: var(--forest-web-pc-login-tab-color, currentColor);
  cursor: pointer;
  font-size: var(--forest-web-pc-login-tab-font-size, 14px);
}

.forest-web-pc-login-panel__tab--active {
  background: var(--forest-web-pc-login-tab-active-background, #111827);
  color: var(--forest-web-pc-login-tab-active-color, #fff);
}
</style>
