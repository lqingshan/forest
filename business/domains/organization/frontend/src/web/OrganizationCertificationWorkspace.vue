<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { adminHttp } from '@forest/http-client'
import { uploadWebFile } from '@forest/file/web/upload'
import type { Certification } from '../shared/types'
import { fetchLatestCertification, submitCertification } from './api'

const props = withDefaults(defineProps<{
  organizationNo?: string
}>(), {
  organizationNo: ''
})

const emit = defineEmits<{
  submitted: [certification: Certification]
}>()

const latestCertification = ref<Certification | null>(null)
const errorMessage = ref('')
const submitting = ref(false)
const uploading = ref(false)
const licenseFileNo = ref('')

const form = reactive({
  companyName: '',
  unifiedSocialCreditCode: '',
  legalRepresentativeName: '',
  contactName: '',
  contactPhone: ''
})

onMounted(loadLatest)
watch(() => props.organizationNo, loadLatest)

async function loadLatest() {
  latestCertification.value = null
  if (!props.organizationNo) {
    return
  }
  errorMessage.value = ''
  try {
    latestCertification.value = await fetchLatestCertification(props.organizationNo)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '认证记录加载失败'
  }
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  uploading.value = true
  errorMessage.value = ''
  try {
    const category = file.type.startsWith('image/') ? 'IMAGE' : 'DOCUMENT'
    const result = await uploadWebFile({ file, fileCategory: category, httpClient: adminHttp, scope: 'admin' })
    licenseFileNo.value = result.fileNo
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '营业执照上传失败'
  } finally {
    uploading.value = false
  }
}

async function handleSubmit() {
  if (!props.organizationNo) {
    errorMessage.value = '请先选择企业'
    return
  }
  if (!licenseFileNo.value) {
    errorMessage.value = '请先上传营业执照'
    return
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    latestCertification.value = await submitCertification(props.organizationNo, {
      ...form,
      businessLicenseFileNo: licenseFileNo.value
    })
    emit('submitted', latestCertification.value)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '认证提交失败'
  } finally {
    submitting.value = false
  }
}

function certificationText(status: string) {
  switch (status) {
    case 'APPROVED':
      return '已通过'
    case 'REJECTED':
      return '已驳回'
    default:
      return '待审核'
  }
}
</script>

<template>
  <section class="workspace-page">
    <header class="workspace-page__header">
      <p class="workspace-page__eyebrow">Certification</p>
      <h1 class="workspace-page__title">企业基础认证</h1>
      <p class="workspace-page__description">本期只做平台内部基础认证，不包含微信/支付宝商户进件字段。</p>
    </header>

    <div v-if="!organizationNo" class="state">请先在企业入口页选择企业。</div>
    <div v-else class="workspace-grid workspace-grid--split">
      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Latest</p>
            <h3>最新认证记录</h3>
          </div>
          <div v-if="latestCertification" class="detail-list">
            <p><span>企业名称</span><strong>{{ latestCertification.companyName }}</strong></p>
            <p><span>信用代码</span><strong>{{ latestCertification.unifiedSocialCreditCode || '-' }}</strong></p>
            <p><span>营业执照</span><strong>{{ latestCertification.businessLicenseFileNo }}</strong></p>
            <p><span>状态</span><strong>{{ certificationText(latestCertification.status) }}</strong></p>
            <p><span>审核备注</span><strong>{{ latestCertification.reviewRemark || '-' }}</strong></p>
          </div>
          <div v-else class="state">暂无认证记录。</div>
        </div>
      </section>

      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Submit</p>
            <h3>提交认证</h3>
          </div>
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
              <input type="file" accept="image/*,.pdf" @change="handleFileChange">
              <small>{{ licenseFileNo || '支持图片或 PDF，上传成功后自动填入 fileNo' }}</small>
            </label>
            <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
            <button type="submit" class="workspace-button" :disabled="submitting || uploading">
              {{ submitting ? '提交中' : uploading ? '上传执照中' : '提交认证' }}
            </button>
          </form>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.stack,
.detail-list {
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

.detail-list p {
  margin: 0;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--workspace-border-soft);
}

.detail-list span {
  color: var(--workspace-text-secondary);
}

.state {
  padding: 16px;
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-soft);
  color: var(--workspace-text-secondary);
}

.error {
  margin: 0;
  color: var(--workspace-danger);
}
</style>
