<script setup lang="ts">
import { ref } from 'vue'
import type { User as CurrentUser } from '../../../shared/user'
import type { WebUserSession } from '../../../web/auth'
import PhonePasswordLoginPanel from '../../../web/auth/components/PhonePasswordLoginPanel.vue'
import PhoneSmsLoginPanel from '../../../web/auth/components/PhoneSmsLoginPanel.vue'
import CarrierOneClickLoginPanel from '../components/CarrierOneClickLoginPanel.vue'
import type { NativeCarrierAuthBridge } from '../bridge'

type LoginMode = 'carrier' | 'password' | 'sms'

const props = withDefaults(defineProps<{
  session: WebUserSession
  bridge?: NativeCarrierAuthBridge
  defaultMode?: LoginMode
  carrierProvider?: string
}>(), {
  defaultMode: 'carrier',
  carrierProvider: undefined
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
  <div class="forest-native-app-h5-login-flow">
    <div class="forest-native-app-h5-login-flow__tabs" role="tablist" aria-label="登录方式">
      <button
        type="button"
        role="tab"
        class="forest-native-app-h5-login-flow__tab"
        :class="{ 'forest-native-app-h5-login-flow__tab--active': activeMode === 'carrier' }"
        :aria-selected="activeMode === 'carrier'"
        @click="activeMode = 'carrier'"
      >
        本机号
      </button>
      <button
        type="button"
        role="tab"
        class="forest-native-app-h5-login-flow__tab"
        :class="{ 'forest-native-app-h5-login-flow__tab--active': activeMode === 'password' }"
        :aria-selected="activeMode === 'password'"
        @click="activeMode = 'password'"
      >
        密码
      </button>
      <button
        type="button"
        role="tab"
        class="forest-native-app-h5-login-flow__tab"
        :class="{ 'forest-native-app-h5-login-flow__tab--active': activeMode === 'sms' }"
        :aria-selected="activeMode === 'sms'"
        @click="activeMode = 'sms'"
      >
        验证码
      </button>
    </div>

    <CarrierOneClickLoginPanel
      v-if="activeMode === 'carrier'"
      :session="session"
      :bridge="bridge"
      :provider="carrierProvider"
      @success="handleSuccess"
    />
    <PhonePasswordLoginPanel
      v-else-if="activeMode === 'password'"
      :session="session"
      @success="handleSuccess"
    />
    <PhoneSmsLoginPanel
      v-else
      :session="session"
      @success="handleSuccess"
    />
  </div>
</template>

<style scoped>
.forest-native-app-h5-login-flow {
  display: grid;
  gap: 16px;
  width: 100%;
}

.forest-native-app-h5-login-flow__tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 4px;
  padding: 4px;
  border: var(--forest-login-tabs-border, 1px solid #d8dee8);
  border-radius: 999px;
  background: var(--forest-login-tabs-background, #f4f6f8);
}

.forest-native-app-h5-login-flow__tab {
  min-width: 0;
  border: 0;
  border-radius: 999px;
  padding: 10px 8px;
  background: transparent;
  color: var(--forest-login-tab-color, currentColor);
  cursor: pointer;
  font-size: 14px;
}

.forest-native-app-h5-login-flow__tab--active {
  background: var(--forest-login-tab-active-background, #111827);
  color: var(--forest-login-tab-active-color, #fff);
}
</style>
