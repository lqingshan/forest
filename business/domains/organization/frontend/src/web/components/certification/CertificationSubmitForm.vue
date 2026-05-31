<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { CertificationSubmitFormPayload } from './certification-form'

withDefaults(defineProps<{
  licenseFileNo?: string
  submitting?: boolean
  uploading?: boolean
  errorMessage?: string
}>(), {
  licenseFileNo: '',
  submitting: false,
  uploading: false,
  errorMessage: ''
})

const emit = defineEmits<{
  upload: [file: File]
  submit: [payload: CertificationSubmitFormPayload]
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const form = reactive({
  companyName: '',
  unifiedSocialCreditCode: '',
  legalRepresentativeName: '',
  contactName: '',
  contactPhone: ''
})

function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) {
    emit('upload', file)
  }
}

function handleSubmit() {
  emit('submit', { ...form })
}

function reset() {
  form.companyName = ''
  form.unifiedSocialCreditCode = ''
  form.legalRepresentativeName = ''
  form.contactName = ''
  form.contactPhone = ''
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

defineExpose({
  reset
})
</script>

<template>
  <form class="stack" @submit.prevent="handleSubmit">
    <label>
      企业名称
      <input v-model.trim="form.companyName" placeholder="请输入企业名称">
    </label>
    <label>
      统一社会信用代码
      <input v-model.trim="form.unifiedSocialCreditCode" placeholder="可重复，不做全局唯一限制">
    </label>
    <label>
      法人姓名
      <input v-model.trim="form.legalRepresentativeName" placeholder="请输入法人姓名">
    </label>
    <label>
      联系人
      <input v-model.trim="form.contactName" placeholder="请输入联系人姓名">
    </label>
    <label>
      联系手机号
      <input v-model.trim="form.contactPhone" placeholder="请输入联系人手机号">
    </label>
    <label>
      营业执照
      <input ref="fileInputRef" type="file" accept="image/*,.pdf" @change="handleFileChange">
      <small>{{ licenseFileNo || '支持图片或 PDF，上传成功后自动填入 fileNo' }}</small>
    </label>
    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <button type="submit" class="workspace-button" :disabled="submitting || uploading">
      {{ submitting ? '提交中' : uploading ? '上传执照中' : '提交认证' }}
    </button>
  </form>
</template>

<style scoped>
.stack {
  display: grid;
  gap: 14px;
}

.stack label {
  display: grid;
  gap: 8px;
  color: var(--workspace-text-secondary);
}

.stack small {
  color: var(--workspace-text-tertiary);
}

.error {
  margin: 0;
  color: var(--workspace-danger);
}
</style>
