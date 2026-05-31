<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { FFieldError } from '@forest/ui-kit'
import { getVerificationErrorMessage, normalizePhone, normalizeSmsCode, sendSmsCode } from '../../shared'
import type { SmsScene } from '../../shared'

const props = withDefaults(defineProps<{
  phone: string
  modelValue: string
  appCode: string
  clientType: string
  accessScope: string
  scene?: SmsScene
  disabled?: boolean
  cooldownSeconds?: number
  codeLength?: number
}>(), {
  scene: 'LOGIN',
  disabled: false,
  cooldownSeconds: 60,
  codeLength: 6
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  sent: [payload: { phone: string, cooldownSeconds: number }]
  senderror: [payload: { message: string }]
}>()

const sending = ref(false)
const secondsLeft = ref(0)
const errorMessage = ref('')
let timer: ReturnType<typeof window.setInterval> | null = null

const normalizedPhone = computed(() => normalizePhone(props.phone))
const canSend = computed(() => !props.disabled && !sending.value && secondsLeft.value <= 0)
const sendButtonText = computed(() => {
  if (sending.value) {
    return '发送中'
  }
  if (secondsLeft.value > 0) {
    return `${secondsLeft.value}s`
  }
  return '获取验证码'
})

function handleCodeInput(event: Event) {
  const target = event.target as HTMLInputElement
  clearErrorMessage()
  emit('update:modelValue', normalizeSmsCode(target.value, props.codeLength))
}

async function handleSendCode() {
  if (!canSend.value) {
    return
  }
  clearErrorMessage()
  if (!normalizedPhone.value) {
    errorMessage.value = '请输入手机号'
    emit('senderror', { message: errorMessage.value })
    return
  }

  sending.value = true
  errorMessage.value = ''
  try {
    const result = await sendSmsCode({
      phone: normalizedPhone.value,
      clientType: props.clientType,
      appCode: props.appCode,
      accessScope: props.accessScope
    })
    const cooldown = resolveCooldownSeconds(result.ttlSeconds)
    startCooldown(cooldown)
    emit('sent', {
      phone: result.phone,
      cooldownSeconds: cooldown
    })
  } catch (error) {
    const message = getVerificationErrorMessage(error)
    errorMessage.value = message
    emit('senderror', { message })
  } finally {
    sending.value = false
  }
}

function clearErrorMessage() {
  errorMessage.value = ''
}

function resolveCooldownSeconds(ttlSeconds?: number) {
  if (typeof ttlSeconds === 'number' && ttlSeconds > 0) {
    return Math.ceil(ttlSeconds)
  }
  return props.cooldownSeconds
}

function startCooldown(seconds: number) {
  stopCooldown()
  secondsLeft.value = seconds
  timer = window.setInterval(() => {
    secondsLeft.value = Math.max(0, secondsLeft.value - 1)
    if (secondsLeft.value <= 0) {
      stopCooldown()
    }
  }, 1000)
}

function stopCooldown() {
  if (timer !== null) {
    window.clearInterval(timer)
    timer = null
  }
}

watch(() => props.phone, clearErrorMessage)

onBeforeUnmount(stopCooldown)
</script>

<template>
  <div class="forest-sms-code-field">
    <div class="forest-sms-code-field__control">
      <input
        class="forest-sms-code-field__input"
        :value="modelValue"
        :maxlength="codeLength"
        inputmode="numeric"
        autocomplete="one-time-code"
        placeholder="请输入验证码"
        :disabled="disabled"
        @input="handleCodeInput"
      >
      <button
        class="forest-sms-code-field__send"
        type="button"
        :disabled="!canSend"
        @click="handleSendCode"
      >
        {{ sendButtonText }}
      </button>
    </div>
    <FFieldError :message="errorMessage" class="forest-sms-code-field__error" />
  </div>
</template>

<style scoped>
.forest-sms-code-field {
  display: grid;
  gap: var(--forest-sms-code-gap, 8px);
}

.forest-sms-code-field__control {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: var(--forest-sms-code-control-gap, 10px);
  align-items: center;
}

.forest-sms-code-field__input {
  min-width: 0;
  width: 100%;
}

.forest-sms-code-field__send {
  min-width: var(--forest-sms-code-send-width, 112px);
  white-space: nowrap;
}

.forest-sms-code-field__error {
  --forest-field-error-color: var(--forest-sms-code-error-color, #c2410c);
  --forest-field-error-font-size: var(--forest-sms-code-error-font-size, 13px);
}
</style>
