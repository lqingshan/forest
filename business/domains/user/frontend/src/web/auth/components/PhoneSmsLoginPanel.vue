<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { FFieldError } from '@forest/ui-kit'
import { SmsCodeField } from '@forest/verification/web/sms'
import type { User as CurrentUser } from '../../../shared/user'
import type { WebLoginHistoryRecord } from '../login-history'
import type { WebUserSession } from '../session-factory'

type PhoneSmsHistoryRecord = Extract<WebLoginHistoryRecord, { mode: 'phone_sms' }>

const props = withDefaults(defineProps<{
  session: WebUserSession
  title?: string
  description?: string
  submitText?: string
}>(), {
  title: '手机号验证码登录',
  description: '输入手机号和验证码完成登录。',
  submitText: '登录'
})

const emit = defineEmits<{
  success: [user: CurrentUser]
}>()

const form = reactive({
  phone: '',
  smsCode: ''
})
const submitting = ref(false)
const errorMessage = ref('')
const loginHistoryRecords = ref<WebLoginHistoryRecord[]>([])
const showingHistory = ref(false)

const phoneSmsHistoryRecords = computed(() => loginHistoryRecords.value.filter(isPhoneSmsHistoryRecord))
const filteredPhoneSmsHistoryRecords = computed(() => {
  const keyword = form.phone.trim()
  if (!keyword) {
    return phoneSmsHistoryRecords.value
  }
  return phoneSmsHistoryRecords.value.filter((record) => record.identifier.includes(keyword))
})
const shouldShowPhoneSmsHistory = computed(() => showingHistory.value && filteredPhoneSmsHistoryRecords.value.length > 0)

function clearErrorMessage() {
  errorMessage.value = ''
}

function refreshLoginHistory() {
  loginHistoryRecords.value = props.session.getLoginHistory().records
}

function handlePhoneFocus() {
  refreshLoginHistory()
  showingHistory.value = true
}

function handlePhoneInput() {
  clearErrorMessage()
  showingHistory.value = true
}

function handlePhoneBlur() {
  showingHistory.value = false
}

function selectPhoneSmsHistory(record: PhoneSmsHistoryRecord) {
  form.phone = record.identifier
  errorMessage.value = ''
  showingHistory.value = false
}

function isPhoneSmsHistoryRecord(record: WebLoginHistoryRecord): record is PhoneSmsHistoryRecord {
  return record.mode === 'phone_sms'
}

function handleSmsCodeInput(value: string) {
  form.smsCode = value
  clearErrorMessage()
}

async function handleSubmit() {
  if (!form.phone.trim()) {
    errorMessage.value = '请输入手机号'
    return
  }
  if (!form.smsCode.trim()) {
    errorMessage.value = '请输入验证码'
    return
  }

  submitting.value = true
  errorMessage.value = ''
  try {
    const user = await props.session.loginWithPhoneSms(form.phone, form.smsCode)
    emit('success', user)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="forest-phone-sms-login-panel" @submit.prevent="handleSubmit">
    <div class="forest-phone-sms-login-panel__heading">
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </div>

    <div class="forest-phone-sms-login-panel__field">
      <span>手机号</span>
      <div class="forest-phone-sms-login-panel__history-field">
        <input
          v-model.trim="form.phone"
          type="tel"
          autocomplete="tel"
          placeholder="请输入手机号"
          :disabled="submitting"
          @focus="handlePhoneFocus"
          @input="handlePhoneInput"
          @blur="handlePhoneBlur"
        >
        <div v-if="shouldShowPhoneSmsHistory" class="forest-phone-sms-login-panel__history" role="listbox">
          <button
            v-for="record in filteredPhoneSmsHistoryRecords"
            :key="record.identifier"
            type="button"
            class="forest-phone-sms-login-panel__history-item"
            role="option"
            @pointerdown.prevent="selectPhoneSmsHistory(record)"
          >
            {{ record.identifier }}
          </button>
        </div>
      </div>
    </div>

    <label class="forest-phone-sms-login-panel__field">
      <span>验证码</span>
      <SmsCodeField
        :model-value="form.smsCode"
        :phone="form.phone"
        :app-code="props.session.appCode"
        :client-type="props.session.clientType"
        :access-scope="props.session.accessScope"
        :disabled="submitting"
        @update:model-value="handleSmsCodeInput"
      />
    </label>

    <FFieldError :message="errorMessage" class="forest-phone-sms-login-panel__error" />

    <button class="forest-phone-sms-login-panel__submit" type="submit" :disabled="submitting">
      {{ submitting ? '登录中' : submitText }}
    </button>
  </form>
</template>

<style scoped>
.forest-phone-sms-login-panel {
  display: grid;
  gap: var(--forest-phone-sms-login-gap, 16px);
  width: 100%;
}

.forest-phone-sms-login-panel__heading {
  display: grid;
  gap: 8px;
}

.forest-phone-sms-login-panel__heading h2,
.forest-phone-sms-login-panel__heading p,
.forest-phone-sms-login-panel__error {
  margin: 0;
}

.forest-phone-sms-login-panel__field {
  display: grid;
  gap: 8px;
}

.forest-phone-sms-login-panel__field span {
  color: var(--forest-phone-sms-login-label-color, currentColor);
  font-size: var(--forest-phone-sms-login-label-font-size, 13px);
}

.forest-phone-sms-login-panel__field input {
  width: 100%;
  min-width: 0;
}

.forest-phone-sms-login-panel__history-field {
  position: relative;
}

.forest-phone-sms-login-panel__history {
  position: absolute;
  z-index: 10;
  top: calc(100% + 6px);
  right: 0;
  left: 0;
  display: grid;
  gap: 4px;
  max-height: 180px;
  overflow: auto;
  padding: 6px;
  border: 1px solid var(--workspace-border, #d8dee8);
  border-radius: 10px;
  background: var(--workspace-surface, #fff);
  box-shadow: 0 16px 36px rgb(15 23 42 / 14%);
}

.forest-phone-sms-login-panel__history-item {
  width: 100%;
  border: 0;
  border-radius: 8px;
  padding: 9px 10px;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font: inherit;
  text-align: left;
}

.forest-phone-sms-login-panel__history-item:hover {
  background: var(--workspace-surface-muted, #f4f6f8);
}

.forest-phone-sms-login-panel__error {
  --forest-field-error-color: var(--forest-phone-sms-login-error-color, #c2410c);
}

.forest-phone-sms-login-panel__submit {
  width: 100%;
}
</style>
