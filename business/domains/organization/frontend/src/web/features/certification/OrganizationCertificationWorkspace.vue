<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { adminHttp } from '@forest/http-client'
import { uploadWebFile } from '@forest/file/web/upload'
import type { Certification } from '../../../shared/types'
import CertificationLatestCard from '../../components/certification/CertificationLatestCard.vue'
import CertificationSubmitForm from '../../components/certification/CertificationSubmitForm.vue'
import type { CertificationSubmitFormPayload } from '../../components/certification/certification-form'
import { fetchLatestCertification, submitCertification } from '../../api/organization-api'

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
const submitFormRef = ref<InstanceType<typeof CertificationSubmitForm> | null>(null)

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

async function handleUpload(file: File) {
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

async function handleSubmit(form: CertificationSubmitFormPayload) {
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
    submitFormRef.value?.reset()
    licenseFileNo.value = ''
    emit('submitted', latestCertification.value)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '认证提交失败'
  } finally {
    submitting.value = false
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
          <CertificationLatestCard :certification="latestCertification" />
        </div>
      </section>

      <section class="workspace-card workspace-card--paper">
        <div class="workspace-card__body">
          <div class="workspace-card__heading">
            <p>Submit</p>
            <h3>提交认证</h3>
          </div>
          <CertificationSubmitForm
            ref="submitFormRef"
            :license-file-no="licenseFileNo"
            :submitting="submitting"
            :uploading="uploading"
            :error-message="errorMessage"
            @upload="handleUpload"
            @submit="handleSubmit"
          />
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.state {
  padding: 16px;
  border-radius: var(--workspace-radius-md);
  background: var(--workspace-surface-soft);
  color: var(--workspace-text-secondary);
}
</style>
