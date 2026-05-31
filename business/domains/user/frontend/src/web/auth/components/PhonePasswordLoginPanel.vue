<script setup lang="ts">
import { computed, ref } from 'vue'
import { FFieldError } from '@forest/ui-kit'
import type { User as CurrentUser } from '../../../shared/user'
import type { WebLoginHistoryRecord } from '../login-history'
import type { WebUserSession } from '../session-factory'

type PhonePasswordHistoryRecord = Extract<WebLoginHistoryRecord, { mode: 'phone_password' }>

const props = withDefaults(defineProps<{
  session: WebUserSession
  title?: string
  description?: string
  submitText?: string
}>(), {
  title: '手机号密码登录',
  description: '输入手机号和密码完成登录。',
  submitText: '登录'
})

const emit = defineEmits<{
  success: [user: CurrentUser]
}>()

const phone = ref('')
const password = ref('')
const savePassword = ref(true)
const submitting = ref(false)
const errorMessage = ref('')
const loginHistoryRecords = ref<WebLoginHistoryRecord[]>([])
const showingHistory = ref(false)

const passwordHistoryRecords = computed(() => loginHistoryRecords.value.filter(isPhonePasswordHistoryRecord))
const filteredPasswordHistoryRecords = computed(() => {
  const keyword = phone.value.trim()
  if (!keyword) {
    return passwordHistoryRecords.value
  }
  return passwordHistoryRecords.value.filter((record) => record.identifier.includes(keyword))
})
const shouldShowPasswordHistory = computed(() => showingHistory.value && filteredPasswordHistoryRecords.value.length > 0)

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

function selectPasswordHistory(record: PhonePasswordHistoryRecord) {
  phone.value = record.identifier
  password.value = record.password ?? ''
  savePassword.value = Boolean(record.password)
  errorMessage.value = ''
  showingHistory.value = false
}

function isPhonePasswordHistoryRecord(record: WebLoginHistoryRecord): record is PhonePasswordHistoryRecord {
  return record.mode === 'phone_password'
}

async function handleSubmit() {
  if (!phone.value || !password.value) {
    errorMessage.value = '请填写手机号和密码'
    return
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    const user = await props.session.loginWithPassword(phone.value, password.value, {
      savePassword: savePassword.value
    })
    emit('success', user)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="forest-password-login-panel" @submit.prevent="handleSubmit">
    <div class="forest-password-login-panel__heading">
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </div>

    <div class="forest-password-login-panel__field">
      <span>手机号</span>
      <div class="forest-password-login-panel__history-field">
        <input
          v-model.trim="phone"
          placeholder="请输入手机号"
          autocomplete="username"
          @focus="handlePhoneFocus"
          @input="handlePhoneInput"
          @blur="handlePhoneBlur"
        >
        <div v-if="shouldShowPasswordHistory" class="forest-password-login-panel__history" role="listbox">
          <button
            v-for="record in filteredPasswordHistoryRecords"
            :key="record.identifier"
            type="button"
            class="forest-password-login-panel__history-item"
            role="option"
            @pointerdown.prevent="selectPasswordHistory(record)"
          >
            <span>{{ record.identifier }}</span>
            <small v-if="!record.password">未保存密码</small>
          </button>
        </div>
      </div>
    </div>
    <label class="forest-password-login-panel__field">
      <span>密码</span>
      <input v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" @input="clearErrorMessage">
    </label>
    <label class="forest-password-login-panel__remember">
      <input v-model="savePassword" type="checkbox" :disabled="submitting">
      <span>保存密码</span>
    </label>
    <FFieldError :message="errorMessage" class="forest-password-login-panel__error" />
    <button type="submit" class="workspace-button" :disabled="submitting">
      {{ submitting ? '登录中' : submitText }}
    </button>
  </form>
</template>

<style scoped>
.forest-password-login-panel {
  display: grid;
  gap: 14px;
  width: 100%;
}

.forest-password-login-panel__heading {
  display: grid;
  gap: 8px;
}

.forest-password-login-panel__heading h2,
.forest-password-login-panel__heading p,
.forest-password-login-panel__error {
  margin: 0;
}

.forest-password-login-panel__field {
  display: grid;
  gap: 8px;
}

.forest-password-login-panel__field span {
  color: var(--workspace-text-secondary);
}

.forest-password-login-panel__field input {
  width: 100%;
  min-width: 0;
}

.forest-password-login-panel__history-field {
  position: relative;
}

.forest-password-login-panel__history {
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

.forest-password-login-panel__history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

.forest-password-login-panel__history-item:hover {
  background: var(--workspace-surface-muted, #f4f6f8);
}

.forest-password-login-panel__history-item small {
  color: var(--workspace-text-tertiary, #7c8798);
  font-size: 12px;
  white-space: nowrap;
}

.forest-password-login-panel__remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  color: var(--workspace-text-secondary);
  font-size: 13px;
  cursor: pointer;
}

.forest-password-login-panel__remember input {
  margin: 0;
}

.forest-password-login-panel__error {
  --forest-field-error-color: var(--workspace-danger);
}
</style>
